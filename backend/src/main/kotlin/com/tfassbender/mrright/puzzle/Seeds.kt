package com.tfassbender.mrright.puzzle

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.random.Random

/**
 * Seeds are free text (e.g. "emma"), hashed into the number the generator's [Random] starts from.
 * SHA-256 instead of [String.hashCode] spreads similar words over very different puzzles.
 */
object Seeds {
    const val MAX_LENGTH: Int = 64
    private const val RANDOM_SEED_LENGTH = 10
    private const val RANDOM_SEED_ALPHABET = "abcdefghijkmnpqrstuvwxyz23456789" // no look-alikes (l/1, o/0)

    private val secureRandom = SecureRandom()

    fun isValid(seed: String): Boolean = seed.isNotBlank() && seed.length <= MAX_LENGTH && seed == seed.trim()

    /** The first 8 bytes of SHA-256(seed) as a `Long`. */
    fun toLong(seed: String): Long {
        val digest = MessageDigest.getInstance("SHA-256").digest(seed.toByteArray(Charsets.UTF_8))
        return ByteBuffer.wrap(digest, 0, Long.SIZE_BYTES).long
    }

    fun toRandom(seed: String): Random = Random(toLong(seed))

    /** A short random seed for players who don't enter one. */
    fun random(): String =
        (1..RANDOM_SEED_LENGTH).map { RANDOM_SEED_ALPHABET[secureRandom.nextInt(RANDOM_SEED_ALPHABET.length)] }
            .joinToString("")
}
