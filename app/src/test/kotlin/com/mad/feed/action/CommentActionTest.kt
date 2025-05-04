package com.mad.feed.actions

import com.mad.feed.dto.DbResponse
import com.mad.feed.models.PostComment
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import java.lang.reflect.Field
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CommentActionTest {
  private lateinit var action: CommentAction

  @BeforeEach
  fun setUp() {
    val config =
        MapApplicationConfig(
            "ktor.database.mode" to "LOCAL",
            "ktor.database.host" to "testhost",
            "ktor.database.port" to "1234")
    action = CommentAction(config)
  }

  @Test
  fun `createComment returns comment on success`() = runBlocking {
    val comment =
        PostComment(
            id = "c1", userId = "u1", content = "test", date = Instant.fromEpochMilliseconds(0))
    val engine = MockEngine { request ->
      if (request.url.encodedPath.endsWith("/create")) {
        respond(
            content = Json.encodeToString(DbResponse(success = true, error = null)),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else {
        respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.createComment("post1", comment)
    assertEquals(comment, result)
  }

  @Test
  fun `createComment throws on failure`() = runBlocking {
    val comment =
        PostComment(
            id = "c1", userId = "u1", content = "test", date = Instant.fromEpochMilliseconds(0))
    val engine = MockEngine { request ->
      if (request.url.encodedPath.endsWith("/create")) {
        respond(
            content = Json.encodeToString(DbResponse(success = false, error = "err")),
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else {
        respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    assertThrows<Throwable> { runBlocking { action.createComment("post1", comment) } }
  }

  private fun injectHttpClient(client: HttpClient) {
    val field: Field = CommentAction::class.java.getDeclaredField("http")
    field.isAccessible = true
    field.set(action, client)
  }
}
