package com.tfassbender.mrright.rest

import com.tfassbender.mrright.auth.AuthError
import com.tfassbender.mrright.auth.AuthException
import com.tfassbender.mrright.auth.AuthService
import com.tfassbender.mrright.auth.RegisterCommand
import com.tfassbender.mrright.auth.UserId
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AuthResource(private val auth: AuthService) {

    @POST
    @Path("/register")
    fun register(request: RegisterRequestDto): Response {
        val outcome = auth.register(RegisterCommand(request.email, request.displayName, request.password))
        return Response.status(Response.Status.CREATED).entity(outcome.toResponse()).build()
    }

    @POST
    @Path("/login")
    fun login(request: LoginRequestDto): AuthResponse = auth.login(request.email, request.password).toResponse()

    @GET
    @Path("/me")
    @RolesAllowed("user")
    fun me(@Context securityContext: SecurityContext): MeResponse =
        auth.me(currentUserId(securityContext)).toMeResponse()

    @POST
    @Path("/verify-email")
    fun verifyEmail(request: TokenRequestDto): AuthResponse = auth.verifyEmail(request.token).toResponse()

    @POST
    @Path("/resend-verification")
    fun resendVerification(request: EmailRequestDto): Response {
        auth.resendVerification(request.email)
        return Response.noContent().build()
    }

    @POST
    @Path("/forgot-password")
    fun forgotPassword(request: EmailRequestDto): Response {
        auth.requestPasswordReset(request.email)
        return Response.noContent().build()
    }

    @POST
    @Path("/reset-password")
    fun resetPassword(request: ResetPasswordRequestDto): AuthResponse =
        auth.resetPassword(request.token, request.newPassword).toResponse()

    @POST
    @Path("/change-password")
    @RolesAllowed("user")
    fun changePassword(@Context securityContext: SecurityContext, request: ChangePasswordRequestDto): AuthResponse =
        auth.changePassword(currentUserId(securityContext), request.currentPassword, request.newPassword).toResponse()

    @PUT
    @Path("/language")
    @RolesAllowed("user")
    fun updateLanguage(@Context securityContext: SecurityContext, request: UpdateLanguageRequestDto): MeResponse =
        auth.updateLanguage(currentUserId(securityContext), request.language).toMeResponse()

    @GET
    @Path("/config")
    fun config(): AuthConfigResponse = AuthConfigResponse(auth.emailDisabled)
}

/** The JWT principal name is the [UserId] (see [com.tfassbender.mrright.auth.JwtTokenIssuer]). */
fun currentUserId(securityContext: SecurityContext): UserId {
    val name = securityContext.userPrincipal?.name
        ?: throw AuthException(AuthError.INVALID_CREDENTIALS, "No authenticated principal.")
    return UserId(name)
}
