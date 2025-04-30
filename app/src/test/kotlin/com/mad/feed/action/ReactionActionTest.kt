package com.mad.feed.actions

import com.mad.feed.dto.DbResponse
import com.mad.feed.models.PostReaction
import com.mad.feed.models.ReactionType
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.TextContent
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import java.lang.reflect.Field
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ReactionActionTest {
  private lateinit var action: ReactionAction

  @BeforeEach
  fun setUp() {
    val config =
        MapApplicationConfig(
            "ktor.database.mode" to "LOCAL",
            "ktor.database.host" to "localhost",
            "ktor.database.port" to "8080")
    action = ReactionAction(config)
  }

  @Test
  fun `addReaction returns reaction when create succeeds`() = runBlocking {
    val reaction = PostReaction(postId = "p1", userId = "u1", reaction = ReactionType.REACTION_LIKE)
    var deleteCalled = false
    val engine = MockEngine { request ->
      when (request.method to request.url.encodedPath) {
        HttpMethod.Delete to "/delete" -> {
          deleteCalled = true
          respond("", HttpStatusCode.OK)
        }
        HttpMethod.Post to "/create" -> {
          val content = request.body as TextContent
          val bodyText = content.text
          assertTrue(bodyText.contains("\"postid\":\"p1\""))
          assertTrue(bodyText.contains("\"userid\":\"u1\""))
          assertTrue(bodyText.contains("\"reaction\":\"REACTION_LIKE\""))
          respond(
              content = Json.encodeToString(DbResponse(success = true, error = null)),
              status = HttpStatusCode.OK,
              headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        }
        else -> respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.addReaction(reaction)
    assertTrue(deleteCalled)
    assertEquals(reaction, result)
  }

  @Test
  fun `addReaction throws when create fails`() = runBlocking {
    val reaction = PostReaction(postId = "p2", userId = "u2", reaction = ReactionType.REACTION_SAD)
    val engine = MockEngine { request ->
      when (request.method to request.url.encodedPath) {
        HttpMethod.Delete to "/delete" -> respond("", HttpStatusCode.OK)
        HttpMethod.Post to "/create" ->
            respond(
                content = Json.encodeToString(DbResponse(success = false, error = "fail")),
                status = HttpStatusCode.OK,
                headers =
                    headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
        else -> respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val ex = assertThrows(Throwable::class.java) { runBlocking { action.addReaction(reaction) } }
    assertTrue(ex.message!!.contains("Failed to add reaction"))
  }

  @Test
  fun `removeReaction returns true when delete succeeds`() = runBlocking {
    val engine = MockEngine { request ->
      if (request.method == HttpMethod.Delete && request.url.encodedPath == "/delete") {
        respond(
            content = Json.encodeToString(DbResponse(success = true, error = null)),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else respondError(HttpStatusCode.NotFound)
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.removeReaction("p1", "u1")
    assertTrue(result)
  }

  @Test
  fun `removeReaction returns false when delete fails`() = runBlocking {
    val engine = MockEngine { request ->
      if (request.method == HttpMethod.Delete && request.url.encodedPath == "/delete") {
        respond(
            content = Json.encodeToString(DbResponse(success = false, error = null)),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else respondError(HttpStatusCode.NotFound)
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.removeReaction("p1", "u1")
    assertFalse(result)
  }

  private fun injectHttpClient(client: HttpClient) {
    val field: Field = ReactionAction::class.java.getDeclaredField("http")
    field.isAccessible = true
    field.set(action, client)
  }
}
