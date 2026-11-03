package com.tfassbender.mrright.game

import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.puzzle.PuzzleConfig
import com.tfassbender.mrright.puzzle.RuleSets
import java.time.Instant
import java.util.UUID

/**
 * A player's game. Only seed + config are stored – the puzzle is regenerated from them on demand.
 * [name] is chosen by the player (the frontend suggests a default); `null` for games created before names existed.
 * [ruleSet] defaults to the book for games created before rule sets existed.
 */
data class Game(
    val id: UUID,
    val ownerId: UserId,
    val seed: String,
    val config: PuzzleConfig,
    val generatorVersion: Int, // the rule set's generator version at creation time
    val createdAt: Instant,
    val state: GameState,
    val name: String? = null,
    val ruleSet: String = RuleSets.BOOK,
) {
    /**
     * The rule set's generator changed since creation (or the rule set is gone), so the puzzle can't be rebuilt –
     * the game can only be deleted.
     */
    val outdated: Boolean get() = generatorVersion != RuleSets.byId(ruleSet)?.generatorVersion

    companion object {
        const val NAME_MAX_LENGTH = 60
    }
}

/**
 * The player's progress.
 * - [crossedOut] / [possibleMatches]: suitors marked as no match / as possible match (starred) – never both.
 * - [finalistsFound]: all non-finalists are crossed out → the tie-break is revealed. Stays true once reached.
 * - [mrRightId]: the correctly picked Mr Right, `null` until solved.
 * - [wrongGuesses]: wrong finalist sets + wrong Mr Right picks.
 * - [version] is incremented by the server on every save; [updatedAt] is the client time of the last change.
 */
data class GameState(
    val crossedOut: Set<Int>,
    val possibleMatches: Set<Int> = emptySet(), // default: games saved before possible matches existed
    val finalistsFound: Boolean,
    val mrRightId: Int?,
    val wrongGuesses: Int,
    val version: Long,
    val updatedAt: Instant,
) {
    val solved: Boolean get() = mrRightId != null

    companion object {
        fun initial(now: Instant): GameState = GameState(emptySet(), emptySet(), false, null, 0, 0, now)
    }
}
