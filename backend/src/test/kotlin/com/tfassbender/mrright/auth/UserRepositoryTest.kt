package com.tfassbender.mrright.auth

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UserRepositoryTest {

    @TempDir
    lateinit var dataDir: Path

    private val user = User(
        id = UserId("abc-123"),
        email = "emma@example.com",
        displayName = "Emma",
        passwordHash = "hash",
        emailVerified = true,
        createdAt = Instant.parse("2026-10-20T10:00:00Z"),
    )

    @Test
    fun `saved user can be found by id, email and display name`() {
        val repository = UserRepository(dataDir)
        repository.save(user)

        assertEquals(user, repository.findById(user.id))
        assertEquals(user, repository.findByEmail(" EMMA@example.com"))
        assertEquals(user, repository.findByDisplayName("emma"))
    }

    @Test
    fun `user survives a new repository instance`() {
        UserRepository(dataDir).save(user)

        assertEquals(user, UserRepository(dataDir).findById(user.id))
    }

    @Test
    fun `unknown and deleted users are not found`() {
        val repository = UserRepository(dataDir)
        repository.save(user)

        assertTrue(repository.delete(user.id))
        assertNull(repository.findById(user.id))
        assertNull(repository.findByEmail("nobody@example.com"))
    }

    @Test
    fun `ids that would escape the data directory are rejected`() {
        assertThrows<IllegalArgumentException> { UserRepository(dataDir).findById(UserId("../evil")) }
    }
}
