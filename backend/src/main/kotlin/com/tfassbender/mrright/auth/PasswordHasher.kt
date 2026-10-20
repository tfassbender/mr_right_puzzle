package com.tfassbender.mrright.auth

import at.favre.lib.crypto.bcrypt.BCrypt

/** BCrypt hashing. Callers must reject passwords over 72 bytes first – BCrypt throws beyond that. */
object PasswordHasher {
    private const val COST = 10

    fun hash(password: String): String =
        BCrypt.withDefaults().hashToString(COST, password.toCharArray())

    fun verify(password: String, hash: String): Boolean =
        BCrypt.verifyer().verify(password.toCharArray(), hash).verified
}
