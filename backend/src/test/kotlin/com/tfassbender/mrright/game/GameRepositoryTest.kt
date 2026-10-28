package com.tfassbender.mrright.game

import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.puzzle.PuzzleConfig
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRepositoryTest {

    @TempDir
    lateinit var dataDir: Path

    private val emma = UserId("emma")
    private val anne = UserId("anne")

    private fun game(owner: UserId, createdAt: String = "2026-10-20T10:00:00Z"): Game {
        val created = Instant.parse(createdAt)
        return Game(UUID.randomUUID(), owner, "seed", PuzzleConfig(100, 2), 1, created, GameState.initial(created))
    }

    @Test
    fun `games saved before possible matches existed still load`() {
        val repository = GameRepository(dataDir)
        val game = game(emma)
        repository.save(game)
        val file = dataDir.resolve("games/${game.id}.json")
        val json = Files.readString(file)
        Files.writeString(file, json.replace(Regex(""""possibleMatches"\s*:\s*\[\s*],?"""), ""))
        check(!Files.readString(file).contains("possibleMatches"))

        assertEquals(game, GameRepository(dataDir).findById(game.id))
    }

    @Test
    fun `saved game can be found by id and owner`() {
        val repository = GameRepository(dataDir)
        val game = game(emma).copy(state = GameState(setOf(3, 1), setOf(5), true, 7, 2, 4, Instant.parse("2026-10-21T10:00:00Z")))
        repository.save(game)

        assertEquals(game, repository.findById(game.id))
        assertEquals(listOf(game), repository.findByOwner(emma))
        assertEquals(emptyList(), repository.findByOwner(anne))
    }

    @Test
    fun `owner index is rebuilt from the files on startup, newest first`() {
        val older = game(emma, "2026-10-20T10:00:00Z")
        val newer = game(emma, "2026-10-22T10:00:00Z")
        GameRepository(dataDir).apply {
            save(older)
            save(newer)
            save(game(anne))
        }

        assertEquals(listOf(newer, older), GameRepository(dataDir).findByOwner(emma))
    }

    @Test
    fun `deleted game is gone`() {
        val repository = GameRepository(dataDir)
        val game = game(emma)
        repository.save(game)

        assertTrue(repository.delete(game))
        assertNull(repository.findById(game.id))
        assertEquals(emptyList(), repository.findByOwner(emma))
    }
}
