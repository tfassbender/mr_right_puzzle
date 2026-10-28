package com.tfassbender.mrright.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.tfassbender.mrright.auth.AuthError
import com.tfassbender.mrright.auth.AuthException
import com.tfassbender.mrright.game.GameConflictException
import com.tfassbender.mrright.game.GameError
import com.tfassbender.mrright.game.GameException
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.ext.ExceptionMapper
import jakarta.ws.rs.ext.Provider
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import java.util.UUID

/**
 * The single place where exceptions become JSON. The [ErrorResponse.code] is the stable contract the
 * frontend translates; framework exceptions (404, the 401 for a missing bearer, …) keep their own response.
 * Anything unexpected becomes a 500 whose details are only logged (and returned in dev).
 */
@Provider
class RestExceptionMapper @Inject constructor(
    @param:ConfigProperty(name = "mrright.errors.include-stack-trace", defaultValue = "false")
    private val includeStackTrace: Boolean,
) : ExceptionMapper<Throwable> {

    private val log = Logger.getLogger(RestExceptionMapper::class.java)

    override fun toResponse(exception: Throwable): Response = when (exception) {
        is AuthException -> error(statusFor(exception.error), exception.error.name, exception.message)
        is GameException -> error(statusFor(exception.error), exception.error.name, exception.message)
        is GameConflictException -> conflict(exception)
        is IllegalArgumentException -> error(Response.Status.BAD_REQUEST, "INVALID_REQUEST", exception.message)
        is WebApplicationException -> exception.response
        else -> internalError(exception)
    }

    private fun internalError(exception: Throwable): Response {
        val errorId = UUID.randomUUID().toString()
        log.errorf(exception, "Unhandled error (id %s)", errorId)
        val body = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = if (includeStackTrace) exception.toString() else "Internal error (id $errorId)",
            errorId = errorId,
            stackTrace = if (includeStackTrace) exception.stackTraceToString() else null,
        )
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build()
    }

    private fun statusFor(error: AuthError): Response.Status = when (error) {
        AuthError.INVALID_EMAIL,
        AuthError.INVALID_LANGUAGE,
        AuthError.INVALID_DISPLAY_NAME,
        AuthError.PASSWORD_TOO_SHORT,
        AuthError.PASSWORD_TOO_LONG,
        AuthError.INVALID_RESET_TOKEN,
        AuthError.INVALID_VERIFICATION_TOKEN,
        -> Response.Status.BAD_REQUEST

        AuthError.MISSING_CREDENTIALS,
        AuthError.INVALID_CREDENTIALS,
        AuthError.EMAIL_NOT_VERIFIED,
        -> Response.Status.UNAUTHORIZED

        AuthError.EMAIL_TAKEN,
        AuthError.DISPLAY_NAME_TAKEN,
        -> Response.Status.CONFLICT

        AuthError.USER_NOT_FOUND -> Response.Status.NOT_FOUND

        AuthError.EMAIL_FEATURES_DISABLED -> Response.Status.SERVICE_UNAVAILABLE
    }

    private fun statusFor(error: GameError): Response.Status = when (error) {
        GameError.INVALID_GAME_CONFIG,
        GameError.INVALID_SEED,
        GameError.INVALID_GAME_STATE,
        -> Response.Status.BAD_REQUEST

        GameError.GAME_NOT_FOUND -> Response.Status.NOT_FOUND
        GameError.GAME_OUTDATED -> Response.Status.GONE
    }

    private fun conflict(exception: GameConflictException): Response =
        Response.status(Response.Status.CONFLICT)
            .entity(ConflictResponse("VERSION_CONFLICT", exception.message.orEmpty(), exception.currentState.toDto()))
            .build()

    private fun error(status: Response.Status, code: String, message: String?): Response =
        Response.status(status)
            .entity(ErrorResponse(code = code, message = message ?: status.reasonPhrase))
            .build()
}

/**
 * Error JSON for the frontend. [code] is translated in the UI, [message] is for logs/dev,
 * [errorId] and [stackTrace] are only set for `INTERNAL_ERROR`.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val code: String,
    val message: String,
    val errorId: String? = null,
    val stackTrace: String? = null,
)
