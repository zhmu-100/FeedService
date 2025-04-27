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

/**
 * Роутер маршрутов для работы с постами
 *
 * Маршруты:
 * - POST /posts - создание нового поста
 * - GET /posts/{id} - получение поста по ID
 * - GET /posts - получение списка постов (общая лента)
 * - GET /posts/user/{userId} - получение списка постов пользователя
 */
fun Route.configurePostRoutes() {
  val postService: PostService by inject()

  route("/posts") {
    /**
     * Создает новый пост.
     *
     * Требуется [CreatePostRequest] в теле запроса. Возвращает статус `201 Created` и созданный
     * пост.
     */
    post {
      val request = call.receive<CreatePostRequest>()
      val post = postService.createPost(request)
      call.respond(HttpStatusCode.Created, post)
    }

    /**
     * Получает пост по его идентификатору.
     *
     * Параметр пути: `id`. Возвращает пост или статус `404 Not Found`, если пост не найден.
     */
    get("/{id}") {
      val id =
          call.parameters["id"]
              ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing post ID")
      val post =
          postService.getPostById(id)
              ?: return@get call.respond(HttpStatusCode.NotFound, "Post not found")
      call.respond(post)
    }

    /**
     * Получает список постов для общего фида.
     *
     * Параметры запроса: `page`, `page_size`. Возвращает список постов в объекте
     * [ListPostsResponse].
     */
    get {
      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 20

      val (posts, _) = postService.listPosts(page, pageSize)
      call.respond(ListPostsResponse(posts = posts))
    }

    /**
     * Получает список постов конкретного пользователя.
     *
     * Параметр пути: `userId`. Параметры запроса: `page`, `page_size`. Возвращает список постов
     * пользователя в объекте [ListPostsResponse].
     */
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
