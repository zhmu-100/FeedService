package com.mad.feed.actions

import com.mad.feed.dto.DbPostRow
import com.mad.feed.dto.DbResponse
import com.mad.feed.models.Post
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.MapApplicationConfig
import java.lang.reflect.Field
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PostActionTest {
  private lateinit var action: PostAction

  @BeforeEach
  fun setUp() {
    val config =
        MapApplicationConfig(
            "ktor.database.mode" to "LOCAL",
            "ktor.database.host" to "localhost",
            "ktor.database.port" to "8080")
    action = PostAction(config)
  }

  @Test
  fun `createPost returns post when DB create succeeds`() = runBlocking {
    val post = Post(id = "p1", userId = "u1", content = "content", attachments = emptyList())
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

    val result = action.createPost(post)
    assertEquals(post, result)
  }

  @Test
  fun `createPost throws when DB create fails`() {
    val post = Post(id = "p1", userId = "u1", content = null, attachments = emptyList())
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

    assertThrows(Throwable::class.java) { runBlocking { action.createPost(post) } }
  }

  @Test
  fun `getPostById returns Post when found`() = runBlocking {
    val id = "p42"
    val dateStr = "2025-04-30T12:00:00Z"
    val date = Instant.parse(dateStr)
    val row = DbPostRow(id = id, userid = "u2", content = "", date = dateStr)
    var callCount = 0
    val engine = MockEngine { request ->
      if (request.url.encodedPath.endsWith("/read")) {
        val content =
            if (callCount++ == 0) {
              Json.encodeToString(ListSerializer(DbPostRow.serializer()), listOf(row))
            } else {
              "[]"
            }
        respond(
            content = content,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else {
        respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.getPostById(id)
    assertNotNull(result)
    assertEquals(id, result!!.id)
    assertEquals("u2", result.userId)
    assertNull(result.content)
    assertEquals(date, result.date)
    assertTrue(result.attachments.isEmpty())
    assertTrue(result.reactions.isEmpty())
    assertTrue(result.comments.isEmpty())
  }

  @Test
  fun `getPostById returns null when not found`() = runBlocking {
    val engine = MockEngine { request ->
      if (request.url.encodedPath.endsWith("/read")) {
        respond(
            content = "[]",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else {
        respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val result = action.getPostById("missing")
    assertNull(result)
  }

  @Test
  fun `listUserPosts returns empty list and zero total when none`() = runBlocking {
    val engine = MockEngine { request ->
      if (request.url.encodedPath.endsWith("/read")) {
        respond(
            content = "[]",
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()))
      } else {
        respondError(HttpStatusCode.NotFound)
      }
    }
    val client = HttpClient(engine) { install(ContentNegotiation) { json() } }
    injectHttpClient(client)

    val (posts, total) = action.listUserPosts("u1", page = 1, pageSize = 5)
    assertTrue(posts.isEmpty())
    assertEquals(0L, total)
  }

  private fun injectHttpClient(client: HttpClient) {
    val field: Field = PostAction::class.java.getDeclaredField("http")
    field.isAccessible = true
    field.set(action, client)
  }
}
