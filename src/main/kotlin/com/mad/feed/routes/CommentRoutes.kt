package com.mad.feed.routes

import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.ListCommentsResponse
import com.mad.feed.models.PaginationRequest
import com.mad.feed.models.PostComment
import com.mad.feed.services.CommentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Роутер маршрутов для работы с комментариями к постам
 *
 * Маршруты:
 * - POST /posts/{postId}/comments - добавить комментарий к посту
 * - GET /posts/{postId}/comments - получить список комментариев к посту
 */
fun Route.configureCommentRoutes() {
  val commentService: CommentService by inject()
  val logger = LoggerProvider.logger

  route("/posts/{postId}/comments") {
    /**
     * Добавляет комментарий к посту
     *
     * Требуется [PostComment] в теле запроса Возвращает статус `201 Created` и добавленный
     * комментарий
     */
    post {
      val postId = call.parameters["postId"]

      if (postId == null) {
        logger.logActivity("API: Ошибка запроса на создание комментария - отсутствует ID поста")
        call.respond(HttpStatusCode.BadRequest, "Missing post ID")
        return@post
      }

      logger.logActivity(
          "API: Запрос на создание комментария", additionalData = mapOf("postId" to postId))

      try {
        val body: PostComment = call.receive()

        logger.logActivity(
            "API: Получены данные комментария",
            additionalData =
                mapOf("postId" to postId, "commentId" to body.id, "userId" to body.userId))

        val createdComment =
            commentService.createComment(CreateCommentRequest(postId = postId, comment = body))

        if (createdComment == null) {
          logger.logActivity(
              "API: Пост не найден при создании комментария",
              additionalData = mapOf("postId" to postId))
          call.respond(HttpStatusCode.NotFound, "Post not found")
          return@post
        }

        logger.logActivity(
            "API: Комментарий успешно создан",
            additionalData = mapOf("postId" to postId, "commentId" to createdComment.id))

        call.respond(HttpStatusCode.Created, createdComment)
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при создании комментария: postId=$postId",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Получает список комментариев к посту
     *
     * Параметры запроса: `page`, `page_size`. Возвращает список комментариев в объекте
     * [ListCommentsResponse].
     */
    get {
      val postId = call.parameters["postId"]

      if (postId == null) {
        logger.logActivity("API: Ошибка запроса на получение комментариев - отсутствует ID поста")
        call.respond(HttpStatusCode.BadRequest, "Missing post ID")
        return@get
      }

      logger.logActivity(
          "API: Запрос на получение комментариев", additionalData = mapOf("postId" to postId))

      try {
        val pagination = call.receive<PaginationRequest>()

        logger.logActivity(
            "API: Получены параметры пагинации",
            additionalData =
                mapOf(
                    "postId" to postId,
                    "page" to pagination.page.toString(),
                    "pageSize" to pagination.pageSize.toString()))

        val comments = commentService.listComments(postId, pagination.page, pagination.pageSize)

        logger.logActivity(
            "API: Комментарии успешно получены",
            additionalData = mapOf("postId" to postId, "commentsCount" to comments.size.toString()))

        call.respond(ListCommentsResponse(comments = comments))
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при получении комментариев: postId=$postId",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }
  }
}
