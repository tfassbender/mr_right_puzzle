package com.tfassbender.mrright.game

import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.auth.UserRepository
import com.tfassbender.mrright.puzzle.Puzzle
import com.tfassbender.mrright.puzzle.PuzzleConfig
import com.tfassbender.mrright.puzzle.RuleSet
import com.tfassbender.mrright.puzzle.RuleSets
import com.tfassbender.mrright.puzzle.Seeds
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * [seed] is optional: `null` or blank means a random seed. [name] is optional too: `null` or blank means none.
 * [ruleSet] `null` or blank means the book.
 */
data class CreateGameCommand(
    val suitorCount: Int,
    val finalistCount: Int,
    val seed: String?,
    val name: String? = null,
    val ruleSet: String? = null,
)

/** A state as sent by the client; the server assigns the version. */
data class StateUpdate(
    val crossedOut: Set<Int>,
    val possibleMatches: Set<Int>,
    val finalistsFound: Boolean,
    val mrRightId: Int?,
    val wrongGuesses: Int,
    val updatedAt: Instant,
)

/** A game with its regenerated puzzle and the hashes for the offline solution check. */
data class LoadedGame(
    val game: Game,
    val puzzle: Puzzle,
    val finalistsHash: String,
    val solutionHash: String,
)

/**
 * Create, list, load, save and delete games. Every rejection throws a [GameException]; a save based on an
 * outdated version throws a [GameConflictException] (optimistic locking).
 *
 * The primary constructor takes all collaborators for container-free unit tests; the `@Inject` one uses the cache
 * and reads the unlocked rule sets from the user file.
 */
@ApplicationScoped
class GameService(
    private val games: GameRepository,
    private val clock: Clock,
    private val puzzleFor: (ruleSetId: String, seed: String, config: PuzzleConfig) -> Puzzle,
    private val idGenerator: () -> UUID,
    private val unlockedRuleSets: (UserId) -> Set<String>,
) {

    @Inject
    constructor(games: GameRepository, clock: Clock, puzzleCache: PuzzleCache, users: UserRepository) :
        this(games, clock, puzzleCache::puzzle, UUID::randomUUID, { users.findById(it)?.unlockedRuleSets.orEmpty() })

    // Serializes saves and deletes of the same game; different games don't block each other.
    private val locks = ConcurrentHashMap<UUID, Any>()

    fun create(ownerId: UserId, command: CreateGameCommand): Game {
        val ruleSet = availableRuleSet(ownerId, command.ruleSet)
        if (!PuzzleConfig.isValid(command.suitorCount, command.finalistCount)) {
            throw GameException(GameError.INVALID_GAME_CONFIG, "Invalid config: $command")
        }
        val seed = command.seed?.trim()?.takeIf { it.isNotEmpty() } ?: Seeds.random()
        if (!Seeds.isValid(seed)) {
            throw GameException(GameError.INVALID_SEED, "Seeds have 1–${Seeds.MAX_LENGTH} characters")
        }
        val name = command.name?.trim()?.takeIf { it.isNotEmpty() }
        if (name != null && name.length > Game.NAME_MAX_LENGTH) {
            throw GameException(GameError.INVALID_GAME_NAME, "Names have at most ${Game.NAME_MAX_LENGTH} characters")
        }
        val now = clock.instant()
        val game = Game(
            id = idGenerator(),
            ownerId = ownerId,
            seed = seed,
            config = PuzzleConfig(command.suitorCount, command.finalistCount),
            generatorVersion = ruleSet.generatorVersion,
            createdAt = now,
            state = GameState.initial(now),
            name = name,
            ruleSet = ruleSet.id,
        )
        games.save(game)
        return game
    }

    /** Newest first. Outdated games are included (marked), so the player can delete them. */
    fun list(ownerId: UserId): List<Game> = games.findByOwner(ownerId)

    fun load(ownerId: UserId, gameId: UUID): LoadedGame {
        val game = playableGame(ownerId, gameId)
        val puzzle = puzzleOf(game)
        return LoadedGame(
            game,
            puzzle,
            SolutionHashes.finalistsHash(game.id, puzzle.finalistIds),
            SolutionHashes.solutionHash(game.id, puzzle.solutionId),
        )
    }

    /** Just the state – what the client polls to pick up changes from another device. */
    fun loadState(ownerId: UserId, gameId: UUID): GameState = playableGame(ownerId, gameId).state

    fun saveState(ownerId: UserId, gameId: UUID, baseVersion: Long, update: StateUpdate): GameState =
        synchronized(lockFor(gameId)) {
            val game = playableGame(ownerId, gameId)
            if (baseVersion != game.state.version) throw GameConflictException(game.state)
            validate(update, puzzleOf(game))

            val state = GameState(
                update.crossedOut, update.possibleMatches, update.finalistsFound, update.mrRightId, update.wrongGuesses,
                version = game.state.version + 1,
                updatedAt = update.updatedAt,
            )
            games.save(game.copy(state = state))
            state
        }

    fun delete(ownerId: UserId, gameId: UUID) {
        synchronized(lockFor(gameId)) {
            games.delete(ownedGame(ownerId, gameId))
        }
        locks.remove(gameId)
    }

    /** The client checks finalists and Mr Right offline via hashes; the server re-checks against the puzzle. */
    private fun validate(update: StateUpdate, puzzle: Puzzle) {
        val suitorIds = 0 until puzzle.config.suitorCount
        val nonFinalists = suitorIds.filterNot { it in puzzle.finalistIds }
        val problem = when {
            update.wrongGuesses < 0 -> "negative wrong guesses"
            update.crossedOut.any { it !in suitorIds } -> "crossed out ids out of range"
            update.possibleMatches.any { it !in suitorIds } -> "possible match ids out of range"
            update.possibleMatches.any { it in update.crossedOut } -> "suitor marked as no match and possible match"
            update.finalistsFound && !update.crossedOut.containsAll(nonFinalists) -> "finalists not found yet"
            update.mrRightId != null && !update.finalistsFound -> "Mr Right picked before the finalists were found"
            update.mrRightId != null && update.mrRightId != puzzle.solutionId -> "wrong Mr Right"
            else -> null
        }
        if (problem != null) throw GameException(GameError.INVALID_GAME_STATE, "Invalid state: $problem")
    }

    private fun availableRuleSet(ownerId: UserId, ruleSetId: String?): RuleSet {
        val id = ruleSetId?.trim()?.takeIf { it.isNotEmpty() } ?: RuleSets.BOOK
        return RuleSets.availableFor(unlockedRuleSets(ownerId)).find { it.id == id }
            ?: throw GameException(GameError.RULE_SET_LOCKED, "Rule set '$id' is not available")
    }

    private fun puzzleOf(game: Game): Puzzle = puzzleFor(game.ruleSet, game.seed, game.config)

    private fun playableGame(ownerId: UserId, gameId: UUID): Game {
        val game = ownedGame(ownerId, gameId)
        if (game.outdated) {
            throw GameException(GameError.GAME_OUTDATED, "Game $gameId uses ${game.ruleSet} generator ${game.generatorVersion}")
        }
        return game
    }

    private fun ownedGame(ownerId: UserId, gameId: UUID): Game =
        games.findById(gameId)?.takeIf { it.ownerId == ownerId }
            ?: throw GameException(GameError.GAME_NOT_FOUND, "Game $gameId not found")

    private fun lockFor(gameId: UUID): Any = locks.computeIfAbsent(gameId) { Any() }
}
