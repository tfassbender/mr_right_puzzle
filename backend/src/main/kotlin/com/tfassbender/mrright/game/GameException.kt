package com.tfassbender.mrright.game

/** A rejected game operation. The [error] name is the stable code the frontend translates. */
class GameException(
    val error: GameError,
    message: String,
) : RuntimeException(message)

enum class GameError {
    INVALID_GAME_CONFIG,
    INVALID_SEED,

    /** Also for games of other players, so ids of foreign games aren't revealed. */
    GAME_NOT_FOUND,

    /** Created with an older generator: the puzzle can't be rebuilt, the game can only be deleted. */
    GAME_OUTDATED,

    /** A saved state that contradicts the puzzle (ids out of range, wrong finalists or Mr Right). */
    INVALID_GAME_STATE,
}

/** A save based on an older version. Carries the current server state, so the client can resolve the conflict. */
class GameConflictException(val currentState: GameState) :
    RuntimeException("Version conflict, server version is ${currentState.version}")
