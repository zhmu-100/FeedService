package com.mad.feed.routes

import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.ListPostsResponse
import com.mad.feed.models.Post
import com.mad.feed.services.PostService
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
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

class PostRoutesTest {
  private val mockPostService = mockk<PostService>()

  private fun Application.testModule() {
    stopKoin() // reset previous Koin
    install(ServerContentNegotiation) { json() }
    install(Koin) { modules(module { single { mockPostService } }) }
    routing { configurePostRoutes() }
  }

  @Test
  fun `POST create post returns 201 Created with body`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val inputPost = Post(id = "orig", userId = "u1", content = "hello")
    val request = CreatePostRequest(post = inputPost)
    val responsePost = inputPost.copy(id = "newId")
    val jsonRequest = Json.encodeToString(CreatePostRequest.serializer(), request)

    coEvery { mockPostService.createPost(request) } returns responsePost

    val response =
        client.post("/posts") {
          contentType(ContentType.Application.Json)
          setBody(jsonRequest)
        }

    assertEquals(HttpStatusCode.Created, response.status)
    val actualPost: Post = response.body()
    assertEquals(responsePost, actualPost)
    coVerify(exactly = 1) { mockPostService.createPost(request) }
  }

  @Test
  fun `GET post by id returns 200 OK when found`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postId = "p1"
    val post = Post(id = postId, userId = "u2")
    coEvery { mockPostService.getPostById(postId) } returns post

    val response = client.get("/posts/$postId")

    assertEquals(HttpStatusCode.OK, response.status)
    val actual: Post = response.body()
    assertEquals(post, actual)
    coVerify(exactly = 1) { mockPostService.getPostById(postId) }
  }

  @Test
  fun `GET post by id returns 404 NotFound when missing`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postId = "missing"
    coEvery { mockPostService.getPostById(postId) } returns null

    val response = client.get("/posts/$postId")

    assertEquals(HttpStatusCode.NotFound, response.status)
    assertEquals("Post not found", response.bodyAsText())
    coVerify(exactly = 1) { mockPostService.getPostById(postId) }
  }

  @Test
  fun `GET posts list returns 200 OK with default pagination`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val postsList = listOf(Post(id = "1", userId = "u1"), Post(id = "2", userId = "u2"))
    coEvery { mockPostService.listPosts(1, 20) } returns Pair(postsList, 10L)

    val response = client.get("/posts")

    assertEquals(HttpStatusCode.OK, response.status)
    val actual: ListPostsResponse = response.body()
    assertEquals(postsList, actual.posts)
    coVerify(exactly = 1) { mockPostService.listPosts(1, 20) }
  }

  @Test
  fun `GET user posts returns 200 OK with default pagination`() = testApplication {
    application { testModule() }
    val client = createClient { install(ContentNegotiation) { json() } }

    val userId = "u3"
    val postsList = listOf(Post(id = "x", userId = userId))
    coEvery { mockPostService.listUserPosts(userId, 1, 20) } returns Pair(postsList, 5L)

    val response = client.get("/posts/user/$userId")

    assertEquals(HttpStatusCode.OK, response.status)
    val actual: ListPostsResponse = response.body()
    assertEquals(postsList, actual.posts)
    coVerify(exactly = 1) { mockPostService.listUserPosts(userId, 1, 20) }
  }
}
