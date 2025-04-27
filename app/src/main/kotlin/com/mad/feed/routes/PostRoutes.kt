package com.mad.feed.routes

import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.ListPostsResponse
import com.mad.feed.services.PostService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configurePostRoutes() {
  val postService: PostService by inject()

  route("/api/posts") {
    // Create a new post
    post {
      val request = call.receive<CreatePostRequest>()
      val post = postService.createPost(request)
      call.respond(HttpStatusCode.Created, post)
    }

    // Get a post by ID
    get("/{id}") {
      val id =
          call.parameters["id"]
              ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing post ID")
      val post =
          postService.getPostById(id)
              ?: return@get call.respond(HttpStatusCode.NotFound, "Post not found")
      call.respond(post)
    }

    // List posts for general feed
    get {
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 20

      val (posts, _) = postService.listPosts(page, pageSize)
      call.respond(ListPostsResponse(posts = posts))
    }

    // List posts by a specific user
    get("/user/{userId}") {
      val userId =
          call.parameters["userId"]
              ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing user ID")
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 20
      val (posts, _) = postService.listUserPosts(userId, page, pageSize)

      call.respond(ListPostsResponse(posts = posts))
    }
  }
}
