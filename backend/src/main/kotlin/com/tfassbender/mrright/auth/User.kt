package com.tfassbender.mrright.auth

import java.time.Instant

/** Opaque user id (a UUID string); also the JWT subject. Serialized as a bare string by jackson-module-kotlin. */
@JvmInline
value class UserId(val value: String)

/**
 * A registered account. Only the BCrypt [passwordHash] is stored, never the password.
 * Verification and reset tokens are emailed in the clear, but only their SHA-256 hashes are persisted,
 * so a leaked user file can't be used to verify or reset an account.
 *
 * [language] is the UI language chosen by the user, saved so it follows them to every device.
 */
data class User(
    val id: UserId,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val emailVerified: Boolean,
    val verificationTokenHash: String? = null,
    val verificationTokenExpiresAt: Instant? = null,
    val passwordResetTokenHash: String? = null,
    val passwordResetTokenExpiresAt: Instant? = null,
    val createdAt: Instant,
    val language: String? = null,
)
