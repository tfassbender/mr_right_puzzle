package com.tfassbender.mrright.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/** Single-use tokens for email verification and password reset: the raw token is emailed, only its hash is stored. */
object AuthTokens {
    private const val TOKEN_BYTES = 32
    private val random = SecureRandom()
    private val encoder = Base64.getUrlEncoder().withoutPadding()

    fun newRawToken(): String {
        val bytes = ByteArray(TOKEN_BYTES)
        random.nextBytes(bytes)
        return encoder.encodeToString(bytes)
    }

    /** SHA-256 (lowercase hex) of a raw token – what gets persisted and looked up. */
    fun hash(rawToken: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(rawToken.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
