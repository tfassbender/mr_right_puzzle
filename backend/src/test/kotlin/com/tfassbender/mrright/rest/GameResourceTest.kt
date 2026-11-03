package com.tfassbender.mrright.rest

import com.tfassbender.mrright.auth.UserId
import com.tfassbender.mrright.auth.UserRepository
import com.tfassbender.mrright.puzzle.BookRuleSet
import com.tfassbender.mrright.puzzle.PuzzleConfig
import com.tfassbender.mrright.puzzle.PuzzleGenerator
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import jakarta.inject.Inject
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class GameResourceTest {

    @Inject
    lateinit var users: UserRepository

    private lateinit var token: String
    private lateinit var userId: String

    @BeforeEach
    fun registerPlayer() {
        val name = "g" + UUID.randomUUID().toString().take(8)
        val response = given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to "$name@example.com", "displayName" to name, "password" to "secret-password"))
            .post("/api/auth/register")
            .then().statusCode(201)
            .extract()
        token = response.path("token")
        userId = response.path("userId")
    }

    private fun asPlayer(): RequestSpecification = given().auth().oauth2(token).contentType(ContentType.JSON)

    private fun createGame(seed: String = "emma"): String =
        asPlayer()
            .body(mapOf("suitorCount" to 100, "finalistCount" to 2, "seed" to seed, "name" to "Sunday puzzle"))
            .post("/api/games")
            .then().statusCode(201)
            .body("seed", equalTo(seed))
            .body("crossedOutCount", equalTo(0))
            .body("outdated", equalTo(false))
            .extract().path("id")

    private fun saveState(id: String, baseVersion: Long, state: Map<String, Any?>) =
        asPlayer().body(mapOf("baseVersion" to baseVersion, "state" to state)).put("/api/games/$id/state")

    @Test
    fun `created game shows up in the list and loads with its puzzle`() {
        val id = createGame()

        asPlayer().get("/api/games").then().statusCode(200)
            .body("", hasSize<Any>(1))
            .body("[0].id", equalTo(id))
            .body("[0].name", equalTo("Sunday puzzle"))
            .body("[0].ruleSet", equalTo("BOOK"))

        asPlayer().get("/api/games/$id").then().statusCode(200)
            .body("suitors", hasSize<Any>(100))
            .body("clues", hasSize<Any>(12))
            .body("clues[0].type", notNullValue())
            .body("ruleSet", equalTo("BOOK"))
            .body("suitors[0].attributes.EYE_COLOR", notNullValue())
            .body("finale", nullValue())
            .body("finalistsHash", notNullValue())
            .body("state.version", equalTo(0))
            .body("name", equalTo("Sunday puzzle"))
    }

    @Test
    fun `saving state uses optimistic locking`() {
        val id = createGame()
        val state = mapOf(
            "crossedOut" to listOf(4, 2), "possibleMatches" to listOf(7), "wrongGuesses" to 0, "updatedAt" to "2026-10-28T10:00:00Z",
        )

        saveState(id, 0, state).then().statusCode(200)
            .body("version", equalTo(1))
            .body("crossedOut", equalTo(listOf(2, 4)))
            .body("possibleMatches", equalTo(listOf(7)))

        saveState(id, 0, state).then().statusCode(409)
            .body("code", equalTo("VERSION_CONFLICT"))
            .body("state.version", equalTo(1))

        asPlayer().get("/api/games/$id/state").then().statusCode(200)
            .body("version", equalTo(1))
            .body("crossedOut", equalTo(listOf(2, 4)))
    }

    @Test
    fun `an unlocked rule set is offered and can be played`() {
        // There is no UI for unlocking – it is edited by hand in the user file, like here.
        val user = users.findById(UserId(userId))!!
        users.save(user.copy(unlockedRuleSets = setOf("EVA")))

        asPlayer().get("/api/auth/me").then().statusCode(200).body("ruleSets", equalTo(listOf("BOOK", "EVA")))
        val id: String = asPlayer()
            .body(mapOf("suitorCount" to 100, "finalistCount" to 2, "ruleSet" to "EVA"))
            .post("/api/games")
            .then().statusCode(201)
            .body("ruleSet", equalTo("EVA"))
            .extract().path("id")
        asPlayer().get("/api/games/$id").then().statusCode(200)
            .body("ruleSet", equalTo("EVA"))
            .body("suitors[0].attributes.HEIGHT", notNullValue())
            .body("clues", hasSize<Any>(12))
            .body("finale.suitor.lastName", equalTo("Faßbender"))
            .body("finale.suitorRating", equalTo(10))
            .body("finale.mrRightRating", notNullValue())
    }

    @Test
    fun `a locked rule set is rejected`() {
        asPlayer()
            .body(mapOf("suitorCount" to 100, "finalistCount" to 2, "ruleSet" to "SECRET"))
            .post("/api/games")
            .then().statusCode(403)
            .body("code", equalTo("RULE_SET_LOCKED"))
    }

    @Test
    fun `a solved state is accepted only with the right Mr Right`() {
        val id = createGame("solve-me")
        val puzzle = PuzzleGenerator.generate(BookRuleSet, "solve-me", PuzzleConfig(100, 2))
        val crossedOut = puzzle.suitors.map { it.id }.filterNot { it in puzzle.finalistIds }
        val wrongId = puzzle.finalistIds.first { it != puzzle.solutionId }
        fun state(mrRightId: Int) = mapOf(
            "crossedOut" to crossedOut, "finalistsFound" to true, "mrRightId" to mrRightId,
            "wrongGuesses" to 1, "updatedAt" to "2026-10-28T10:00:00Z",
        )

        saveState(id, 0, state(wrongId)).then().statusCode(400).body("code", equalTo("INVALID_GAME_STATE"))
        saveState(id, 0, state(puzzle.solutionId)).then().statusCode(200).body("solved", equalTo(true))
    }

    @Test
    fun `invalid config is rejected with an error code`() {
        asPlayer().body(mapOf("suitorCount" to 10, "finalistCount" to 2)).post("/api/games")
            .then().statusCode(400).body("code", equalTo("INVALID_GAME_CONFIG"))
    }

    @Test
    fun `deleted game is gone, unknown ids are not found`() {
        val id = createGame()

        asPlayer().delete("/api/games/$id").then().statusCode(204)
        asPlayer().get("/api/games/$id").then().statusCode(404).body("code", equalTo("GAME_NOT_FOUND"))
        asPlayer().get("/api/games/not-a-uuid").then().statusCode(404)
    }

    @Test
    fun `games require a login`() {
        given().get("/api/games").then().statusCode(401)
    }

    @Test
    fun `responses are compressed when the client accepts it`() {
        val id = createGame()

        asPlayer().header("Accept-Encoding", "gzip").get("/api/games/$id")
            .then().statusCode(200).header("Content-Encoding", "gzip")
    }
}
