package com.tfassbender.mrright.rest

import com.tfassbender.mrright.game.Game
import com.tfassbender.mrright.game.GameState
import com.tfassbender.mrright.game.LoadedGame
import com.tfassbender.mrright.game.StateUpdate
import com.tfassbender.mrright.puzzle.Clue
import com.tfassbender.mrright.puzzle.Suitor
import java.time.Instant

data class CreateGameRequestDto(val suitorCount: Int = 0, val finalistCount: Int = 0, val seed: String? = null)

/** [updatedAt] defaults to the epoch, so a missing value can't fail deserialization. */
data class StateDto(
    val crossedOut: Set<Int> = emptySet(),
    val finalistsFound: Boolean = false,
    val mrRightId: Int? = null,
    val wrongGuesses: Int = 0,
    val updatedAt: Instant = Instant.EPOCH,
) {
    fun toUpdate(): StateUpdate = StateUpdate(crossedOut, finalistsFound, mrRightId, wrongGuesses, updatedAt)
}

data class SaveStateRequestDto(val baseVersion: Long = -1, val state: StateDto = StateDto())

data class GameStateDto(
    val crossedOut: List<Int>,
    val finalistsFound: Boolean,
    val mrRightId: Int?,
    val solved: Boolean,
    val wrongGuesses: Int,
    val version: Long,
    val updatedAt: Instant,
)

/** One entry of the game list – no puzzle, so listing doesn't regenerate anything. */
data class GameSummaryDto(
    val id: String,
    val seed: String,
    val suitorCount: Int,
    val finalistCount: Int,
    val createdAt: Instant,
    val outdated: Boolean,
    val crossedOutCount: Int,
    val finalistsFound: Boolean,
    val solved: Boolean,
    val wrongGuesses: Int,
    val updatedAt: Instant,
)

/** The full puzzle plus state. The finalists and Mr Right are only included as hashes. */
data class GameDto(
    val id: String,
    val seed: String,
    val suitorCount: Int,
    val finalistCount: Int,
    val generatorVersion: Int,
    val createdAt: Instant,
    val suitors: List<Suitor>,
    val clues: List<Clue>,
    val finalistsHash: String,
    val solutionHash: String,
    val state: GameStateDto,
)

/** The 409 body: the current server state, so the client can resolve the conflict. */
data class ConflictResponse(val code: String, val message: String, val state: GameStateDto)

fun GameState.toDto(): GameStateDto =
    GameStateDto(crossedOut.sorted(), finalistsFound, mrRightId, solved, wrongGuesses, version, updatedAt)

fun Game.toSummary(): GameSummaryDto = GameSummaryDto(
    id.toString(), seed, config.suitorCount, config.finalistCount, createdAt, outdated,
    state.crossedOut.size, state.finalistsFound, state.solved, state.wrongGuesses, state.updatedAt,
)

fun LoadedGame.toDto(): GameDto = GameDto(
    game.id.toString(), game.seed, game.config.suitorCount, game.config.finalistCount, game.generatorVersion,
    game.createdAt, puzzle.suitors, puzzle.clues, finalistsHash, solutionHash, game.state.toDto(),
)
