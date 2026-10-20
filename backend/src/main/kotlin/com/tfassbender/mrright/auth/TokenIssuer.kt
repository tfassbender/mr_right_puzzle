package com.tfassbender.mrright.auth

import io.smallrye.jwt.build.Jwt
import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.time.Duration

/** Issues session tokens. An interface so [AuthService] can be unit-tested without signing keys. */
interface TokenIssuer {
    fun issue(userId: UserId): String
}

/**
 * Signs an HS256 JWT. The [UserId] is both `upn` (so `SecurityContext.userPrincipal.name` returns it) and
 * `subject`; the `user` group grants `@RolesAllowed("user")`. Verification is done by Quarkus from `mp.jwt.*`.
 */
@ApplicationScoped
class JwtTokenIssuer(
    @param:ConfigProperty(name = "mp.jwt.verify.issuer")
    private val issuer: String,
    @param:ConfigProperty(name = "mrright.auth.jwt.expiration-hours", defaultValue = "720")
    private val expirationHours: Long,
) : TokenIssuer {

    override fun issue(userId: UserId): String =
        Jwt.issuer(issuer)
            .upn(userId.value)
            .subject(userId.value)
            .groups(setOf("user"))
            .expiresIn(Duration.ofHours(expirationHours))
            .sign()
}
