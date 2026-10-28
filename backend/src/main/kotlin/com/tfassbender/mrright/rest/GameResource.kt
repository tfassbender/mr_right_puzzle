package com.tfassbender.mrright.rest

import com.tfassbender.mrright.game.CreateGameCommand
import com.tfassbender.mrright.game.GameError
import com.tfassbender.mrright.game.GameException
import com.tfassbender.mrright.game.GameService
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.SecurityContext
import java.util.UUID

@Path("/api/games")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("user")
class GameResource(private val games: GameService) {

    @GET
    fun list(@Context securityContext: SecurityContext): List<GameSummaryDto> =
        games.list(currentUserId(securityContext)).map { it.toSummary() }

    @POST
    fun create(@Context securityContext: SecurityContext, request: CreateGameRequestDto): Response {
        val command = CreateGameCommand(request.suitorCount, request.finalistCount, request.seed, request.name)
        val game = games.create(currentUserId(securityContext), command)
        return Response.status(Response.Status.CREATED).entity(game.toSummary()).build()
    }

    @GET
    @Path("/{id}")
    fun load(@Context securityContext: SecurityContext, @PathParam("id") id: String): GameDto =
        games.load(currentUserId(securityContext), gameId(id)).toDto()

    @GET
    @Path("/{id}/state")
    fun loadState(@Context securityContext: SecurityContext, @PathParam("id") id: String): GameStateDto =
        games.loadState(currentUserId(securityContext), gameId(id)).toDto()

    @PUT
    @Path("/{id}/state")
    fun saveState(
        @Context securityContext: SecurityContext,
        @PathParam("id") id: String,
        request: SaveStateRequestDto,
    ): GameStateDto =
        games.saveState(currentUserId(securityContext), gameId(id), request.baseVersion, request.state.toUpdate()).toDto()

    @DELETE
    @Path("/{id}")
    fun delete(@Context securityContext: SecurityContext, @PathParam("id") id: String): Response {
        games.delete(currentUserId(securityContext), gameId(id))
        return Response.noContent().build()
    }

    // A malformed id can't belong to any game – same answer as for an unknown one.
    private fun gameId(id: String): UUID = try {
        UUID.fromString(id)
    } catch (_: IllegalArgumentException) {
        throw GameException(GameError.GAME_NOT_FOUND, "Malformed game id '$id'")
    }
}
