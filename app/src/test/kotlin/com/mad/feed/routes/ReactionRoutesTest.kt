package com.mad.feed.routes

import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction
import com.mad.feed.models.ReactionType
import com.mad.feed.models.RemoveReactionRequest
import com.mad.feed.services.ReactionService
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class ReactionRoutesTest {
  private val mockReactionService = mockk<ReactionService>()

  private fun Application.testModule() {
    install(ContentNegotiation) { json() }
    install(Koin) { modules(module { single { mockReactionService } }) }
    routing { configureReactionRoutes() }
  }

  @Test
  fun `POST add reaction returns 201 Created`() = testApplication {
    application { testModule() }

    val postId = "postA"
    val requestBody =
        AddReactionRequest(postId = postId, userId = "u1", reaction = ReactionType.REACTION_LIKE)
    val jsonRequest = Json.encodeToString(AddReactionRequest.serializer(), requestBody)
    val expectedResponse =
        PostReaction(postId = postId, userId = "u1", reaction = ReactionType.REACTION_LIKE)
    val expectedJson = Json.encodeToString(PostReaction.serializer(), expectedResponse)

    coEvery { mockReactionService.addReaction(postId, requestBody) } returns expectedResponse

    val response =
        client.post("/posts/$postId/reactions") {
          contentType(ContentType.Application.Json)
          setBody(jsonRequest)
        }

    assertEquals(HttpStatusCode.Created, response.status)
    assertEquals(expectedJson, response.bodyAsText())
    coVerify(exactly = 1) { mockReactionService.addReaction(postId, requestBody) }
  }

  @Test
  fun `DELETE remove reaction returns 204 NoContent when success`() = testApplication {
    application { testModule() }

    val postId = "postB"
    val requestBody = RemoveReactionRequest(postId = postId, userId = "u2")
    val jsonRequest = Json.encodeToString(RemoveReactionRequest.serializer(), requestBody)

    coEvery { mockReactionService.removeReaction(postId, requestBody.userId) } returns true

    val response =
        client.delete("/posts/$postId/reactions") {
          contentType(ContentType.Application.Json)
          setBody(jsonRequest)
        }

    assertEquals(HttpStatusCode.NoContent, response.status)
    assertEquals("", response.bodyAsText())
    coVerify(exactly = 1) { mockReactionService.removeReaction(postId, requestBody.userId) }
  }

  @Test
  fun `DELETE remove reaction returns 404 NotFound when not found`() = testApplication {
    application { testModule() }

    val postId = "postC"
    val requestBody = RemoveReactionRequest(postId = postId, userId = "u3")
    val jsonRequest = Json.encodeToString(RemoveReactionRequest.serializer(), requestBody)

    coEvery { mockReactionService.removeReaction(postId, requestBody.userId) } returns false

    val response =
        client.delete("/posts/$postId/reactions") {
          contentType(ContentType.Application.Json)
          setBody(jsonRequest)
        }

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertEquals("Reaction not found", response.bodyAsText())
    coVerify(exactly = 1) { mockReactionService.removeReaction(postId, requestBody.userId) }
  }
}
