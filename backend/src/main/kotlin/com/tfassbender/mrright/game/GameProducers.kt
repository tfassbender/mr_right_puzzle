package com.tfassbender.mrright.game

import com.tfassbender.mrright.puzzle.Puzzle
import com.tfassbender.mrright.puzzle.PuzzleConfig
import com.tfassbender.mrright.puzzle.PuzzleGenerator
import com.tfassbender.mrright.storage.DataDirectory
import io.quarkus.cache.CacheResult
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

/** Turns the plain [GameRepository] into a bean; the class itself stays unit-testable against a temp dir. */
@ApplicationScoped
class GameProducers {

    @Produces
    @ApplicationScoped
    fun gameRepository(dataDirectory: DataDirectory): GameRepository = GameRepository(dataDirectory.path)
}

/** Regenerating a 10,000-suitor puzzle on every load would be wasteful, so recent puzzles stay in memory. */
@ApplicationScoped
class PuzzleCache {

    @CacheResult(cacheName = "puzzles")
    fun puzzle(seed: String, config: PuzzleConfig): Puzzle = PuzzleGenerator.generate(seed, config)
}
