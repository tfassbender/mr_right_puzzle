package com.tfassbender.mrright.auth

import com.fasterxml.jackson.module.kotlin.readValue
import com.tfassbender.mrright.storage.AtomicJsonFile
import com.tfassbender.mrright.storage.JsonMapper
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

/**
 * Stores one `<dataDir>/users/<userId>.json` per account. Lookups other than by id scan the directory,
 * which is fine for a hobby-sized user base; an in-memory index is the obvious optimization if needed.
 */
class UserRepository(dataDir: Path) {

    private val usersDir: Path = dataDir.resolve("users")
    private val mapper = JsonMapper.create()

    fun save(user: User) {
        AtomicJsonFile.write(userFile(user.id.value), mapper.writeValueAsString(user))
    }

    fun findById(id: UserId): User? = readUser(userFile(id.value))

    /** Emails are stored normalized (trimmed, lowercase). */
    fun findByEmail(email: String): User? {
        val normalized = email.trim().lowercase()
        return allUsers().firstOrNull { it.email == normalized }
    }

    /** Display names keep their case but must be unique ignoring case. */
    fun findByDisplayName(displayName: String): User? =
        allUsers().firstOrNull { it.displayName.equals(displayName, ignoreCase = true) }

    fun findByVerificationTokenHash(tokenHash: String): User? =
        allUsers().firstOrNull { it.verificationTokenHash == tokenHash }

    fun findByPasswordResetTokenHash(tokenHash: String): User? =
        allUsers().firstOrNull { it.passwordResetTokenHash == tokenHash }

    fun delete(id: UserId): Boolean = Files.deleteIfExists(userFile(id.value))

    private fun allUsers(): Sequence<User> {
        if (!usersDir.exists()) return emptySequence()
        return usersDir.listDirectoryEntries("*.json").asSequence().mapNotNull(::readUser)
    }

    private fun readUser(file: Path): User? {
        if (!file.exists()) return null
        return try {
            mapper.readValue<User>(file.readText())
        } catch (e: IOException) {
            throw UncheckedIOException("Failed to read user file '$file'", e)
        }
    }

    // The id ends up in a file path, so a crafted id must not escape the data directory.
    private fun userFile(userId: String): Path {
        require(userId.isNotBlank()) { "userId must not be blank" }
        require(userId.none { it == '/' || it == '\\' } && userId != "." && userId != "..") {
            "userId must not contain path separators: '$userId'"
        }
        return usersDir.resolve("$userId.json")
    }
}
