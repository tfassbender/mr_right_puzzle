package com.tfassbender.mrright.rest

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import java.util.UUID

@QuarkusTest
class AuthResourceTest {

    private fun uniqueName() = "u" + UUID.randomUUID().toString().take(8)

    private fun register(name: String): String =
        given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to "$name@example.com", "displayName" to name, "password" to "secret-password"))
            .post("/api/auth/register")
            .then()
            .statusCode(201)
            .body("token", notNullValue())
            .extract().path("token")

    @Test
    fun `register then me returns the account`() {
        val name = uniqueName()
        val token = register(name)

        given()
            .auth().oauth2(token)
            .get("/api/auth/me")
            .then()
            .statusCode(200)
            .body("displayName", equalTo(name))
            .body("email", equalTo("$name@example.com"))
    }

    @Test
    fun `login with a wrong password returns the error code`() {
        val name = uniqueName()
        register(name)

        given()
            .contentType(ContentType.JSON)
            .body(mapOf("email" to "$name@example.com", "password" to "wrong-password"))
            .post("/api/auth/login")
            .then()
            .statusCode(401)
            .body("code", equalTo("INVALID_CREDENTIALS"))
    }

    @Test
    fun `me without a token is unauthorized`() {
        given().get("/api/auth/me").then().statusCode(401)
    }

    @Test
    fun `config reports email disabled in test`() {
        given().get("/api/auth/config").then().statusCode(200).body("emailDisabled", equalTo(true))
    }

    @Test
    fun `health is public`() {
        given().get("/api/health").then().statusCode(200).body("status", equalTo("UP"))
    }
}
