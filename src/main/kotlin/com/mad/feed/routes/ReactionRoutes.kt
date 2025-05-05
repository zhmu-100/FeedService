package com.mad.feed.routes

import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.RemoveReactionRequest
import com.mad.feed.services.ReactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

/**
 * Роутер маршрутов для работы с реакциями к постам
 *
 * Маршруты:
 * - POST /posts/{postId}/reactions - добавить реакцию к посту
 * - DELETE /posts/{postId}/reactions - удалить реакцию к посту
 */
fun Route.configureReactionRoutes() {
  val reactionService: ReactionService by inject()
  val logger = LoggerProvider.logger

  route("/posts/{postId}/reactions") {
    /**
     * Добавляет реакцию к посту
     *
     * Требуется [AddReactionRequest] в теле запроса Возвращает статус `201 Created` и добавленную
     * реакцию
     */
    post {
      val postId = call.parameters["postId"]

      if (postId == null) {
        logger.logActivity("API: Ошибка запроса на добавление реакции - отсутствует ID поста")
        call.respond(HttpStatusCode.BadRequest, "Missing post ID")
        return@post
      }

      logger.logActivity(
          "API: Запрос на добавление реакции", additionalData = mapOf("postId" to postId))

      try {
        val request = call.receive<AddReactionRequest>()

        logger.logActivity(
            "API: Получены данные реакции",
            additionalData =
                mapOf(
                    "postId" to postId,
                    "userId" to request.userId,
                    "reaction" to request.reaction.name))

        val reaction = reactionService.addReaction(postId, request)

        logger.logActivity(
            "API: Реакция успешно добавлена",
            additionalData =
                mapOf(
                    "postId" to postId,
                    "userId" to request.userId,
                    "reaction" to request.reaction.name))

        call.respond(HttpStatusCode.Created, reaction)
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при добавлении реакции: postId=$postId",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }

    /**
     * Удаляет реакцию пользователя с поста
     *
     * Требуется [RemoveReactionRequest] в теле запроса Возвращает статус `204 No Content` при
     * успехе или `404 Not Found`, если реакция не найдена
     */
    delete {
      val postId = call.parameters["postId"]

      if (postId == null) {
        logger.logActivity("API: Ошибка запроса на удаление реакции - отсутствует ID поста")
        call.respond(HttpStatusCode.BadRequest, "Missing post ID")
        return@delete
      }

      logger.logActivity(
          "API: Запрос на удаление реакции", additionalData = mapOf("postId" to postId))

      try {
        val request = call.receive<RemoveReactionRequest>()

        logger.logActivity(
            "API: Получены данные для удаления реакции",
            additionalData = mapOf("postId" to postId, "userId" to request.userId))

        val success = reactionService.removeReaction(postId, request.userId)

        if (success) {
          logger.logActivity(
              "API: Реакция успешно удалена",
              additionalData = mapOf("postId" to postId, "userId" to request.userId))
          call.respond(HttpStatusCode.NoContent)
        } else {
          logger.logActivity(
              "API: Реакция не найдена",
              additionalData = mapOf("postId" to postId, "userId" to request.userId))
          call.respond(HttpStatusCode.NotFound, "Reaction not found")
        }
      } catch (e: Exception) {
        logger.logError(
            "API: Ошибка при удалении реакции: postId=$postId",
            errorMessage = e.message ?: "Неизвестная ошибка",
            stackTrace = e.stackTraceToString())
        throw e
      }
    }
  }
}
