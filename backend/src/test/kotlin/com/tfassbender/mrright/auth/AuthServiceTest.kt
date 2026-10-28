package com.tfassbender.mrright.auth

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthServiceTest {

    @TempDir
    lateinit var dataDir: Path

    private val clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZoneOffset.UTC)
    private val emails = RecordingEmailSender()
    private var nextId = 0

    private fun service(emailDisabled: Boolean) = AuthService(
        users = UserRepository(dataDir),
        emailSender = emails,
        tokenIssuer = object : TokenIssuer {
            override fun issue(userId: UserId) = "token-${userId.value}"
        },
        clock = clock,
        emailDisabled = emailDisabled,
        verificationTtlMinutes = 60,
        resetTtlMinutes = 30,
        idGenerator = { "user-${nextId++}" },
    )

    private val emma = RegisterCommand("Emma@Example.com ", "Emma", "secret-password")

    @Test
    fun `register with email disabled returns a token and normalizes the email`() {
        val outcome = service(emailDisabled = true).register(emma)

        assertEquals("emma@example.com", outcome.user.email)
        assertTrue(outcome.user.emailVerified)
        assertEquals("token-user-0", outcome.token)
    }

    @Test
    fun `register with email enabled sends a verification mail and returns no token`() {
        val outcome = service(emailDisabled = false).register(emma)

        assertNull(outcome.token)
        assertFalse(outcome.user.emailVerified)
        assertEquals(1, emails.verificationTokens.size)
    }

    @Test
    fun `verification token from the mail verifies the account`() {
        val auth = service(emailDisabled = false)
        auth.register(emma)

        val outcome = auth.verifyEmail(emails.verificationTokens.single())

        assertTrue(outcome.user.emailVerified)
        assertNotNull(outcome.token)
    }

    @Test
    fun `duplicate email and display name are rejected`() {
        val auth = service(emailDisabled = true)
        auth.register(emma)

        val emailTaken = assertThrows<AuthException> { auth.register(emma.copy(displayName = "Other")) }
        val nameTaken = assertThrows<AuthException> { auth.register(emma.copy(email = "x@example.com", displayName = "EMMA")) }

        assertEquals(AuthError.EMAIL_TAKEN, emailTaken.error)
        assertEquals(AuthError.DISPLAY_NAME_TAKEN, nameTaken.error)
    }

    @Test
    fun `login rejects a wrong password`() {
        val auth = service(emailDisabled = true)
        auth.register(emma)

        val error = assertThrows<AuthException> { auth.login("emma@example.com", "wrong-password") }

        assertEquals(AuthError.INVALID_CREDENTIALS, error.error)
    }

    @Test
    fun `login of an unverified account is rejected when email is enabled`() {
        val auth = service(emailDisabled = false)
        auth.register(emma)

        val error = assertThrows<AuthException> { auth.login("emma@example.com", "secret-password") }

        assertEquals(AuthError.EMAIL_NOT_VERIFIED, error.error)
    }

    @Test
    fun `password reset sets the new password`() {
        val auth = service(emailDisabled = false)
        auth.register(emma)
        auth.requestPasswordReset("emma@example.com")

        auth.resetPassword(emails.resetTokens.single(), "new-password")

        assertNotNull(auth.login("emma@example.com", "new-password").token)
    }

    @Test
    fun `invalid input is rejected`() {
        val auth = service(emailDisabled = true)

        assertEquals(AuthError.INVALID_EMAIL, assertThrows<AuthException> { auth.register(emma.copy(email = "no-at")) }.error)
        assertEquals(AuthError.INVALID_DISPLAY_NAME, assertThrows<AuthException> { auth.register(emma.copy(displayName = "Em")) }.error)
        assertEquals(AuthError.PASSWORD_TOO_SHORT, assertThrows<AuthException> { auth.register(emma.copy(password = "short")) }.error)
    }

    @Test
    fun `a display name may have just three characters`() {
        val auth = service(emailDisabled = true)

        assertEquals("Ann", auth.register(emma.copy(displayName = "Ann")).user.displayName)
    }

    @Test
    fun `language is saved to the account`() {
        val auth = service(emailDisabled = true)
        val user = auth.register(emma).user

        auth.updateLanguage(user.id, "de")

        assertEquals("de", auth.me(user.id).language)
        assertEquals(AuthError.INVALID_LANGUAGE, assertThrows<AuthException> { auth.updateLanguage(user.id, "german") }.error)
    }

    private class RecordingEmailSender : EmailSender {
        val verificationTokens = mutableListOf<String>()
        val resetTokens = mutableListOf<String>()

        override fun sendVerification(email: String, displayName: String, rawToken: String) {
            verificationTokens += rawToken
        }

        override fun sendPasswordReset(email: String, displayName: String, rawToken: String) {
            resetTokens += rawToken
        }
    }
}
