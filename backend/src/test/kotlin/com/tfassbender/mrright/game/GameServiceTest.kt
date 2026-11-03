package com.tfassbender.mrright.game

import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.puzzle.Puzzle
import com.tfassbender.mrright.puzzle.BookRuleSet
import com.tfassbender.mrright.puzzle.PuzzleConfig
import com.tfassbender.mrright.puzzle.PuzzleGenerator
import com.tfassbender.mrright.puzzle.RuleSets
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameServiceTest {

    @TempDir
    lateinit var dataDir: Path

    private val emma = UserId("emma")
    private val now = Instant.parse("2026-10-28T10:00:00Z")
    private lateinit var repository: GameRepository
    private lateinit var service: GameService
    private var unlocked: Set<String> = emptySet()

    @BeforeEach
    fun setUp() {
        repository = GameRepository(dataDir)
        val puzzleFor = { ruleSet: String, seed: String, config: PuzzleConfig ->
            PuzzleGenerator.generate(RuleSets.byId(ruleSet)!!, seed, config)
        }
        service = GameService(repository, Clock.fixed(now, ZoneOffset.UTC), puzzleFor, UUID::randomUUID) { unlocked }
    }

    private fun createGame(): Game = service.create(emma, CreateGameCommand(100, 2, "emma"))

    private fun update(
        crossedOut: Set<Int> = emptySet(),
        finalistsFound: Boolean = false,
        mrRightId: Int? = null,
        possibleMatches: Set<Int> = emptySet(),
    ) = StateUpdate(crossedOut, possibleMatches, finalistsFound, mrRightId, wrongGuesses = 1, updatedAt = now.plusSeconds(60))

    private fun nonFinalists(puzzle: Puzzle): Set<Int> = puzzle.suitors.map { it.id }.filterNot { it in puzzle.finalistIds }.toSet()

    @Test
    fun `created game uses the current generator and an initial state`() {
        val game = createGame()

        assertEquals("emma", game.seed)
        assertEquals(RuleSets.BOOK, game.ruleSet)
        assertEquals(BookRuleSet.generatorVersion, game.generatorVersion)
        assertEquals(GameState.initial(now), game.state)
        assertEquals(listOf(game), service.list(emma))
    }

    @Test
    fun `blank seed is replaced by a random one, invalid input is rejected`() {
        val game = service.create(emma, CreateGameCommand(100, 2, "  "))
        assertTrue(game.seed.isNotBlank())

        assertEquals(GameError.INVALID_GAME_CONFIG, assertThrows<GameException> { service.create(emma, CreateGameCommand(50, 2, null)) }.error)
        assertEquals(GameError.INVALID_SEED, assertThrows<GameException> { service.create(emma, CreateGameCommand(100, 2, "x".repeat(65))) }.error)
    }

    @Test
    fun `the name is optional, trimmed and limited in length`() {
        assertEquals("Sunday", service.create(emma, CreateGameCommand(100, 2, null, "  Sunday ")).name)
        assertNull(service.create(emma, CreateGameCommand(100, 2, null, " ")).name)

        val tooLong = CreateGameCommand(100, 2, null, "x".repeat(Game.NAME_MAX_LENGTH + 1))
        assertEquals(GameError.INVALID_GAME_NAME, assertThrows<GameException> { service.create(emma, tooLong) }.error)
    }

    @Test
    fun `loaded game carries hashes of the finalists and Mr Right`() {
        val loaded = service.load(emma, createGame().id)

        assertEquals(SolutionHashes.finalistsHash(loaded.game.id, loaded.puzzle.finalistIds), loaded.finalistsHash)
        assertEquals(SolutionHashes.solutionHash(loaded.game.id, loaded.puzzle.solutionId), loaded.solutionHash)
    }

    @Test
    fun `other players' games are not found`() {
        val game = createGame()

        assertEquals(GameError.GAME_NOT_FOUND, assertThrows<GameException> { service.load(UserId("anne"), game.id) }.error)
        assertEquals(GameError.GAME_NOT_FOUND, assertThrows<GameException> { service.delete(UserId("anne"), game.id) }.error)
    }

    @Test
    fun `saving increments the version, an old base version is a conflict`() {
        val game = createGame()

        val saved = service.saveState(emma, game.id, 0, update(crossedOut = setOf(1, 2)))
        assertEquals(1, saved.version)
        assertEquals(setOf(1, 2), saved.crossedOut)
        assertEquals(now.plusSeconds(60), saved.updatedAt)

        val conflict = assertThrows<GameConflictException> { service.saveState(emma, game.id, 0, update()) }
        assertEquals(saved, conflict.currentState)
    }

    @Test
    fun `finalists and Mr Right are re-checked against the puzzle`() {
        val game = createGame()
        val puzzle = service.load(emma, game.id).puzzle
        val allButFinalists = nonFinalists(puzzle)
        val wrongId = puzzle.finalistIds.first { it != puzzle.solutionId }

        assertInvalid(game, update(crossedOut = allButFinalists - allButFinalists.first(), finalistsFound = true))
        assertInvalid(game, update(crossedOut = allButFinalists, finalistsFound = true, mrRightId = wrongId))
        assertInvalid(game, update(mrRightId = puzzle.solutionId))
        assertInvalid(game, update(crossedOut = setOf(100)))
        assertInvalid(game, update(possibleMatches = setOf(100)))
        assertInvalid(game, update(crossedOut = setOf(1), possibleMatches = setOf(1)))

        val solved = service.saveState(emma, game.id, 0, update(allButFinalists, true, puzzle.solutionId, puzzle.finalistIds))
        assertEquals(puzzle.finalistIds, solved.possibleMatches)
        assertTrue(solved.solved)
    }

    @Test
    fun `the book is the default, other rule sets need to be unlocked`() {
        assertEquals(RuleSets.BOOK, service.create(emma, CreateGameCommand(100, 2, null, ruleSet = " ")).ruleSet)
        assertEquals(RuleSets.BOOK, service.create(emma, CreateGameCommand(100, 2, null, ruleSet = RuleSets.BOOK)).ruleSet)

        // Unknown and locked rule sets are rejected alike – even when an unknown id is "unlocked" in the user file.
        unlocked = setOf("SECRET")
        val exception = assertThrows<GameException> { service.create(emma, CreateGameCommand(100, 2, null, ruleSet = "SECRET")) }
        assertEquals(GameError.RULE_SET_LOCKED, exception.error)
    }

    @Test
    fun `an unlocked rule set can be played`() {
        unlocked = setOf("EVA")
        val game = service.create(emma, CreateGameCommand(100, 2, "eva", ruleSet = "EVA"))

        assertEquals("EVA", game.ruleSet)
        assertEquals(12, service.load(emma, game.id).puzzle.clues.size)
    }

    @Test
    fun `games of a removed rule set are outdated`() {
        val game = createGame().copy(ruleSet = "REMOVED")
        repository.save(game)

        assertTrue(service.list(emma).single().outdated)
    }

    @Test
    fun `outdated games can only be deleted`() {
        val game = createGame().copy(generatorVersion = 0)
        repository.save(game)

        assertTrue(service.list(emma).single().outdated)
        assertEquals(GameError.GAME_OUTDATED, assertThrows<GameException> { service.load(emma, game.id) }.error)
        assertEquals(GameError.GAME_OUTDATED, assertThrows<GameException> { service.saveState(emma, game.id, 0, update()) }.error)
        service.delete(emma, game.id)
        assertEquals(emptyList(), service.list(emma))
    }

    private fun assertInvalid(game: Game, update: StateUpdate) {
        val exception = assertThrows<GameException> { service.saveState(emma, game.id, 0, update) }
        assertEquals(GameError.INVALID_GAME_STATE, exception.error)
    }
}
