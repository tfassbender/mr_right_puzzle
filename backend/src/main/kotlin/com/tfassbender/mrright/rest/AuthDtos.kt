package com.tfassbender.mrright.rest

import com.tfassbender.mrright.auth.AuthOutcome
import com.tfassbender.mrright.auth.User
import com.tfassbender.mrright.puzzle.RuleSets

// Request fields default to "" so a missing value reaches the service validation as a clean rejection
// instead of a deserialization error.

data class RegisterRequestDto(val email: String = "", val displayName: String = "", val password: String = "")

data class LoginRequestDto(val email: String = "", val password: String = "")

data class TokenRequestDto(val token: String = "")

data class EmailRequestDto(val email: String = "")

data class ResetPasswordRequestDto(val token: String = "", val newPassword: String = "")

data class ChangePasswordRequestDto(val currentPassword: String = "", val newPassword: String = "")

data class UpdateLanguageRequestDto(val language: String = "")

/**
 * Result of register/login/verify/reset/change. [token] is `null` only while email verification is pending.
 * [ruleSets] are the rule set ids the user may create games with – always including the book.
 */
data class AuthResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val emailVerified: Boolean,
    val language: String?,
    val ruleSets: List<String>,
    val token: String?,
)

data class MeResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val emailVerified: Boolean,
    val language: String?,
    val ruleSets: List<String>,
)

/** Public flags the SPA reads on start, e.g. to hide the email-only flows in dev. */
data class AuthConfigResponse(val emailDisabled: Boolean)

fun User.toMeResponse(): MeResponse = MeResponse(id.value, email, displayName, emailVerified, language, availableRuleSets())

fun AuthOutcome.toResponse(): AuthResponse = AuthResponse(
    user.id.value, user.email, user.displayName, user.emailVerified, user.language, user.availableRuleSets(), token,
)

private fun User.availableRuleSets(): List<String> = RuleSets.availableFor(unlockedRuleSets).map { it.id }
