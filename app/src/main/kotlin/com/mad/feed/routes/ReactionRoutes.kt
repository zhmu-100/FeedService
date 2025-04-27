package com.mad.feed.routes

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

  route("/posts/{postId}/reactions") {
    /**
     * Добавляет реакцию к посту
     *
     * Требуется [AddReactionRequest] в теле запроса Возвращает статус `201 Created` и добавленную
     * реакцию
     */
    post {
      val postId =
          call.parameters["postId"]
              ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing post ID")
      val request = call.receive<AddReactionRequest>()

      val reaction = reactionService.addReaction(postId, request)
      call.respond(HttpStatusCode.Created, reaction)
    }

    /**
     * Удаляет реакцию пользователя с поста
     *
     * Требуется [RemoveReactionRequest] в теле запроса Возвращает статус `204 No Content` при
     * успехе или `404 Not Found`, если реакция не найдена
     */
    delete {
      val postId =
          call.parameters["postId"]
              ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing post ID")
      val request = call.receive<RemoveReactionRequest>()

      val success = reactionService.removeReaction(postId, request.userId)
      if (success) {
        call.respond(HttpStatusCode.NoContent)
      } else {
        call.respond(HttpStatusCode.NotFound, "Reaction not found")
      }
    }
  }
}
