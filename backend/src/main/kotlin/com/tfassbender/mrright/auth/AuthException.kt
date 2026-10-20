package com.tfassbender.mrright.auth

/**
 * Thrown when an auth operation is rejected. The [error] name is the stable code the frontend translates;
 * the message is for logs and tests only.
 */
class AuthException(
    val error: AuthError,
    message: String,
) : RuntimeException(message)

enum class AuthError {
    INVALID_EMAIL,

    /** 4–15 chars: letters incl. äöüÄÖÜß, digits, `_`, `-`. */
    INVALID_DISPLAY_NAME,
    PASSWORD_TOO_SHORT,

    /** BCrypt only hashes 72 bytes; longer passwords are rejected instead of failing with a 500. */
    PASSWORD_TOO_LONG,
    EMAIL_TAKEN,
    DISPLAY_NAME_TAKEN,
    MISSING_CREDENTIALS,

    /** Deliberately the same for "unknown user" and "wrong password". */
    INVALID_CREDENTIALS,
    INVALID_RESET_TOKEN,
    INVALID_VERIFICATION_TOKEN,
    EMAIL_NOT_VERIFIED,

    /** An email flow was called while email features are disabled (dev/test). */
    EMAIL_FEATURES_DISABLED,
    USER_NOT_FOUND,
    INVALID_LANGUAGE,
}
