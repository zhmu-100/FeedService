package com.mad.feed.routes

import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.ListCommentsResponse
import com.mad.feed.models.PaginationRequest
import com.mad.feed.models.PostComment
import com.mad.feed.services.CommentService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class CommentRoutesTest {
  private val mockCommentService = mockk<CommentService>()

  private fun Application.testModule() {
    // Reset Koin between tests
    stopKoin()
    install(ServerContentNegotiation) { json() }
    install(Koin) { modules(module { single { mockCommentService } }) }
    routing { configureCommentRoutes() }
  }

  @Test
  fun `POST comment returns 201 Created when service returns comment`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postId = "post123"
    val original =
        PostComment(
            id = "orig",
            userId = "userA",
            content = "Hello",
            date = Instant.fromEpochMilliseconds(0))
    val requestJson = Json.encodeToString(PostComment.serializer(), original)
    val created = original.copy(id = "newId", date = Instant.fromEpochMilliseconds(1))
    coEvery { mockCommentService.createComment(CreateCommentRequest(postId, original)) } returns
        created

    val response: HttpResponse =
        client.post("/posts/$postId/comments") {
          contentType(ContentType.Application.Json)
          setBody(requestJson)
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val actual: PostComment = response.body()
    assertEquals(created, actual)
    coVerify(exactly = 1) {
      mockCommentService.createComment(CreateCommentRequest(postId, original))
    }
  }

  @Test
  fun `POST comment returns 404 NotFound when service returns null`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postId = "post123"
    val original =
        PostComment(
            id = "orig",
            userId = "userA",
            content = "Hello",
            date = Instant.fromEpochMilliseconds(0))
    val requestJson = Json.encodeToString(PostComment.serializer(), original)
    coEvery { mockCommentService.createComment(any()) } returns null

    val response: HttpResponse =
        client.post("/posts/$postId/comments") {
          contentType(ContentType.Application.Json)
          setBody(requestJson)
        }

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertEquals("Post not found", response.bodyAsText())
    coVerify(exactly = 1) {
      mockCommentService.createComment(CreateCommentRequest(postId, original))
    }
  }

  @Test
  fun `GET comments returns 200 OK with list`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postId = "post456"
    val pagination = PaginationRequest(page = 2, pageSize = 3)
    val paginationJson = Json.encodeToString(PaginationRequest.serializer(), pagination)
    val comments =
        listOf(
            PostComment(
                id = "c1", userId = "u1", content = "A", date = Instant.fromEpochMilliseconds(10)),
            PostComment(
                id = "c2", userId = "u2", content = "B", date = Instant.fromEpochMilliseconds(20)))
    coEvery {
      mockCommentService.listComments(postId, pagination.page, pagination.pageSize)
    } returns comments

    val response: HttpResponse =
        client.get("/posts/$postId/comments") {
          contentType(ContentType.Application.Json)
          setBody(paginationJson)
        }

    assertEquals(HttpStatusCode.OK, response.status)
    val actual: ListCommentsResponse = response.body()
    assertEquals(comments, actual.comments)
    coVerify(exactly = 1) {
      mockCommentService.listComments(postId, pagination.page, pagination.pageSize)
    }
  }
}
