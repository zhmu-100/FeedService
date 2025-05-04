package com.mad.feed.routes

import com.mad.feed.logging.LoggerProvider
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
  val logger = LoggerProvider.logger

  route("/posts") {
    /**
     * Создает новый пост.
     *
     * Требуется [CreatePostRequest] в теле запроса. Возвращает статус `201 Created` и созданный
     * пост.
     */
    post {
      logger.logActivity("API: Запрос на создание поста")

      try {
        val request = call.receive<CreatePostRequest>()

        logger.logActivity(
            "API: Получены данные поста",
            additionalData =
                mapOf(
                    "userId" to request.post.userId,
                    "attachmentsCount" to request.post.attachments.size.toString()))

        val post = postService.createPost(request)

        logger.logActivity(
            "API: Пост успешно создан",
            additionalData = mapOf("postId" to post.id, "userId" to post.userId))

        call.respond(HttpStatusCode.Created, post)
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при создании поста",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получает пост по его идентификатору.
     *
     * Параметр пути: `id`. Возвращает пост или статус `404 Not Found`, если пост не найден.
     */
    get("/{id}") {
      val id = call.parameters["id"]

      if (id == null) {
        logger.logActivity("API: Ошибка запроса на получение поста - отсутствует ID")
        call.respond(HttpStatusCode.BadRequest, "Missing post ID")
        return@get
      }

      logger.logActivity("API: Запрос на получение поста", additionalData = mapOf("postId" to id))

      try {
        val post = postService.getPostById(id)

        if (post == null) {
          logger.logActivity("API: Пост не найден", additionalData = mapOf("postId" to id))
          call.respond(HttpStatusCode.NotFound, "Post not found")
          return@get
        }

        logger.logActivity(
            "API: Пост успешно получен",
            additionalData = mapOf("postId" to id, "userId" to post.userId))

        call.respond(post)
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при получении поста: postId=$id",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
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

      logger.logActivity(
          "API: Запрос на получение списка постов",
          additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))

      try {
        val (posts, total) = postService.listPosts(page, pageSize)

        logger.logActivity(
            "API: Список постов успешно получен",
            additionalData =
                mapOf("postsCount" to posts.size.toString(), "totalPosts" to total.toString()))

        call.respond(ListPostsResponse(posts = posts))
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при получении списка постов: page=$page, pageSize=$pageSize",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получает список постов конкретного пользователя.
     *
     * Параметр пути: `userId`. Параметры запроса: `page`, `page_size`. Возвращает список постов
     * пользователя в объекте [ListPostsResponse].
     */
    get("/user/{userId}") {
      val userId = call.parameters["userId"]

      if (userId == null) {
        logger.logActivity(
            "API: Ошибка запроса на получение постов пользователя - отсутствует ID пользователя")
        call.respond(HttpStatusCode.BadRequest, "Missing user ID")
        return@get
      }

      val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
      val pageSize = call.request.queryParameters["page_size"]?.toIntOrNull() ?: 20

      logger.logActivity(
          "API: Запрос на получение постов пользователя",
          additionalData =
              mapOf(
                  "userId" to userId, "page" to page.toString(), "pageSize" to pageSize.toString()))

      try {
        val (posts, total) = postService.listUserPosts(userId, page, pageSize)

        logger.logActivity(
            "API: Посты пользователя успешно получены",
            additionalData =
                mapOf(
                    "userId" to userId,
                    "postsCount" to posts.size.toString(),
                    "totalPosts" to total.toString()))

        call.respond(ListPostsResponse(posts = posts))
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при получении постов пользователя: userId=$userId, page=$page, pageSize=$pageSize",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }
  }
}
