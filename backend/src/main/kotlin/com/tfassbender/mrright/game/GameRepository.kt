package com.tfassbender.mrright.game

import com.fasterxml.jackson.module.kotlin.readValue
import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.storage.AtomicJsonFile
import com.tfassbender.mrright.storage.JsonMapper
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

/**
 * Stores one `<dataDir>/games/<gameId>.json` per game. The game files are scanned once on startup into an
 * in-memory index `ownerId → gameIds`, so listing a player's games doesn't read every file.
 * Serializing concurrent saves of the same game is the [GameService]'s job.
 */
class GameRepository(dataDir: Path) {

    private val gamesDir: Path = dataDir.resolve("games")
    private val mapper = JsonMapper.create()
    private val ownerIndex = ConcurrentHashMap<UserId, MutableSet<UUID>>()

    init {
        if (gamesDir.exists()) {
            gamesDir.listDirectoryEntries("*.json").mapNotNull(::readGame).forEach(::index)
        }
    }

    fun save(game: Game) {
        AtomicJsonFile.write(gameFile(game.id), mapper.writeValueAsString(game))
        index(game)
    }

    fun findById(id: UUID): Game? = readGame(gameFile(id))

    fun findByOwner(ownerId: UserId): List<Game> =
        ownerIndex[ownerId].orEmpty().mapNotNull(::findById).sortedByDescending { it.createdAt }

    fun delete(game: Game): Boolean {
        ownerIndex[game.ownerId]?.remove(game.id)
        return Files.deleteIfExists(gameFile(game.id))
    }

    private fun index(game: Game) {
        ownerIndex.computeIfAbsent(game.ownerId) { ConcurrentHashMap.newKeySet() }.add(game.id)
    }

    private fun readGame(file: Path): Game? {
        if (!file.exists()) return null
        return try {
            mapper.readValue<Game>(file.readText())
        } catch (e: IOException) {
            throw UncheckedIOException("Failed to read game file '$file'", e)
        }
    }

    // A UUID can't contain path separators, so it can't escape the data directory.
    private fun gameFile(id: UUID): Path = gamesDir.resolve("$id.json")
}
