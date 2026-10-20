package com.tfassbender.mrright.auth

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Clock
import java.time.Instant
import java.util.UUID

data class RegisterCommand(
    val email: String,
    val displayName: String,
    val password: String,
)

/** A successful auth operation. [token] is `null` only for a registration still pending email verification. */
data class AuthOutcome(
    val user: User,
    val token: String?,
)

/**
 * Register, login, email verification and password reset (taken over from `avalons_final_stand`).
 * Every rejection throws an [AuthException].
 *
 * With [emailDisabled] (dev/test) registration auto-verifies and returns a token, login skips the
 * verified check, and the email flows refuse with [AuthError.EMAIL_FEATURES_DISABLED].
 * [resendVerification] and [requestPasswordReset] succeed silently for unknown accounts, so they don't
 * reveal which emails are registered.
 *
 * The primary constructor takes all collaborators for container-free unit tests; the `@Inject` one reads config.
 */
@ApplicationScoped
class AuthService(
    private val users: UserRepository,
    private val emailSender: EmailSender,
    private val tokenIssuer: TokenIssuer,
    private val clock: Clock,
    val emailDisabled: Boolean,
    private val verificationTtlMinutes: Long,
    private val resetTtlMinutes: Long,
    private val idGenerator: () -> String,
) {

    @Inject
    constructor(
        users: UserRepository,
        emailSender: EmailSender,
        tokenIssuer: TokenIssuer,
        clock: Clock,
        @ConfigProperty(name = "mrright.auth.email-disabled", defaultValue = "false")
        emailDisabled: Boolean,
        @ConfigProperty(name = "mrright.auth.email-verification.token-ttl-minutes", defaultValue = "1440")
        verificationTtlMinutes: Long,
        @ConfigProperty(name = "mrright.auth.password-reset.token-ttl-minutes", defaultValue = "30")
        resetTtlMinutes: Long,
    ) : this(
        users, emailSender, tokenIssuer, clock, emailDisabled, verificationTtlMinutes, resetTtlMinutes,
        idGenerator = { UUID.randomUUID().toString() },
    )

    fun register(command: RegisterCommand): AuthOutcome {
        val email = normalizeEmail(command.email)
        validateEmail(email)
        validateDisplayName(command.displayName)
        validatePassword(command.password)

        if (users.findByEmail(email) != null) {
            throw AuthException(AuthError.EMAIL_TAKEN, "Email already registered: $email")
        }
        if (users.findByDisplayName(command.displayName) != null) {
            throw AuthException(AuthError.DISPLAY_NAME_TAKEN, "Display name already taken: ${command.displayName}")
        }

        val now = clock.instant()
        val user = User(
            id = UserId(idGenerator()),
            email = email,
            displayName = command.displayName,
            passwordHash = PasswordHasher.hash(command.password),
            emailVerified = emailDisabled,
            createdAt = now,
        )
        if (emailDisabled) {
            users.save(user)
            return AuthOutcome(user, tokenIssuer.issue(user.id))
        }

        // No session until the emailed link is followed.
        val rawToken = AuthTokens.newRawToken()
        val pending = user.copy(
            verificationTokenHash = AuthTokens.hash(rawToken),
            verificationTokenExpiresAt = now.plusSeconds(verificationTtlMinutes * 60),
        )
        users.save(pending)
        emailSender.sendVerification(pending.email, pending.displayName, rawToken)
        return AuthOutcome(pending, token = null)
    }

    fun login(email: String, password: String): AuthOutcome {
        if (email.isBlank() || password.isEmpty()) {
            throw AuthException(AuthError.MISSING_CREDENTIALS, "Email and password are required.")
        }
        val user = users.findByEmail(normalizeEmail(email))
        if (user == null || !PasswordHasher.verify(password, user.passwordHash)) {
            throw AuthException(AuthError.INVALID_CREDENTIALS, "Invalid email or password.")
        }
        if (!emailDisabled && !user.emailVerified) {
            throw AuthException(AuthError.EMAIL_NOT_VERIFIED, "Email not verified: ${user.email}")
        }
        return AuthOutcome(user, tokenIssuer.issue(user.id))
    }

    fun me(userId: UserId): User = requireUser(userId)

    fun verifyEmail(rawToken: String): AuthOutcome {
        requireEmailEnabled()
        val user = users.findByVerificationTokenHash(AuthTokens.hash(rawToken))
        if (user == null || isExpired(user.verificationTokenExpiresAt)) {
            throw AuthException(AuthError.INVALID_VERIFICATION_TOKEN, "Invalid or expired verification token.")
        }
        val verified = user.copy(emailVerified = true, verificationTokenHash = null, verificationTokenExpiresAt = null)
        users.save(verified)
        return AuthOutcome(verified, tokenIssuer.issue(verified.id))
    }

    fun resendVerification(email: String) {
        requireEmailEnabled()
        val user = users.findByEmail(normalizeEmail(email)) ?: return
        if (user.emailVerified) return

        val rawToken = AuthTokens.newRawToken()
        val updated = user.copy(
            verificationTokenHash = AuthTokens.hash(rawToken),
            verificationTokenExpiresAt = clock.instant().plusSeconds(verificationTtlMinutes * 60),
        )
        users.save(updated)
        emailSender.sendVerification(updated.email, updated.displayName, rawToken)
    }

    fun requestPasswordReset(email: String) {
        requireEmailEnabled()
        val user = users.findByEmail(normalizeEmail(email)) ?: return

        val rawToken = AuthTokens.newRawToken()
        val updated = user.copy(
            passwordResetTokenHash = AuthTokens.hash(rawToken),
            passwordResetTokenExpiresAt = clock.instant().plusSeconds(resetTtlMinutes * 60),
        )
        users.save(updated)
        emailSender.sendPasswordReset(updated.email, updated.displayName, rawToken)
    }

    fun resetPassword(rawToken: String, newPassword: String): AuthOutcome {
        requireEmailEnabled()
        validatePassword(newPassword)
        val user = users.findByPasswordResetTokenHash(AuthTokens.hash(rawToken))
        if (user == null || isExpired(user.passwordResetTokenExpiresAt)) {
            throw AuthException(AuthError.INVALID_RESET_TOKEN, "Invalid or expired reset token.")
        }
        val updated = user.copy(
            passwordHash = PasswordHasher.hash(newPassword),
            // Following the reset link proves email ownership, so it also verifies the address.
            emailVerified = true,
            passwordResetTokenHash = null,
            passwordResetTokenExpiresAt = null,
        )
        users.save(updated)
        return AuthOutcome(updated, tokenIssuer.issue(updated.id))
    }

    fun changePassword(userId: UserId, currentPassword: String, newPassword: String): AuthOutcome {
        validatePassword(newPassword)
        val user = requireUser(userId)
        if (!PasswordHasher.verify(currentPassword, user.passwordHash)) {
            throw AuthException(AuthError.INVALID_CREDENTIALS, "Current password is incorrect.")
        }
        val updated = user.copy(passwordHash = PasswordHasher.hash(newPassword))
        users.save(updated)
        return AuthOutcome(updated, tokenIssuer.issue(updated.id))
    }

    fun updateLanguage(userId: UserId, language: String): User {
        if (!LANGUAGE_PATTERN.matches(language)) {
            throw AuthException(AuthError.INVALID_LANGUAGE, "Not a language code: $language")
        }
        val updated = requireUser(userId).copy(language = language)
        users.save(updated)
        return updated
    }

    private fun requireUser(userId: UserId): User =
        users.findById(userId) ?: throw AuthException(AuthError.USER_NOT_FOUND, "No user with id ${userId.value}")

    private fun requireEmailEnabled() {
        if (emailDisabled) {
            throw AuthException(AuthError.EMAIL_FEATURES_DISABLED, "Email features are disabled.")
        }
    }

    private fun isExpired(expiresAt: Instant?): Boolean =
        expiresAt == null || clock.instant().isAfter(expiresAt)

    private fun validateEmail(email: String) {
        if (!EMAIL_PATTERN.matches(email)) {
            throw AuthException(AuthError.INVALID_EMAIL, "Not a valid email address: $email")
        }
    }

    private fun validateDisplayName(displayName: String) {
        if (!DISPLAY_NAME_PATTERN.matches(displayName)) {
            throw AuthException(AuthError.INVALID_DISPLAY_NAME, "Invalid display name: $displayName")
        }
    }

    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw AuthException(AuthError.PASSWORD_TOO_SHORT, "Password must be at least $MIN_PASSWORD_LENGTH characters.")
        }
        // Counted in UTF-8 bytes: umlauts and emoji cost more than one.
        if (password.toByteArray(Charsets.UTF_8).size > MAX_PASSWORD_BYTES) {
            throw AuthException(AuthError.PASSWORD_TOO_LONG, "Password must be at most $MAX_PASSWORD_BYTES bytes.")
        }
    }

    private fun normalizeEmail(email: String): String = email.trim().lowercase()

    companion object {
        const val MIN_PASSWORD_LENGTH = 8
        const val MAX_PASSWORD_BYTES = 72

        // No whitespace, exactly one '@', and a dotted domain.
        private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

        private val DISPLAY_NAME_PATTERN = Regex("^[A-Za-z0-9_äöüÄÖÜß-]{4,15}$")

        // A locale code like `en`, `de-AT` or `zh-Hant`.
        private val LANGUAGE_PATTERN = Regex("^[a-z]{2,3}(-[A-Za-z]{2,4})?$")
    }
}
