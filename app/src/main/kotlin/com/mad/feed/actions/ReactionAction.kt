package com.mad.feed.actions

import com.mad.feed.dto.*
import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.PostReaction
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация интерфейса [IReactionAction] для работы с реакциями на посты
 *
 * Выполняет HTTP-запросы к сервису базы данных для создания и удаления реакций
 *
 * Реализует методы:
 * - [addReaction]
 * - [removeReaction]
 */
class ReactionAction : IReactionAction {
  private val logger = LoggerProvider.logger
  private val dotenv = dotenv()
  private val dbMode = dotenv["DB_MODE"] ?: "LOCAL"
  private val dbHost = dotenv["DB_HOST"] ?: "localhost"
  private val dbPort = dotenv["DB_PORT"] ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"

  private val http = HttpClient { install(ContentNegotiation) { json() } }

  /**
   * Добавляет реакцию к посту
   *
   * Предварительно удаляет существующую реакцию пользователя на тот же пост
   *
   * @param reaction Реакция для добавления
   * @return Добавленная реакция
   */
  override suspend fun addReaction(reaction: PostReaction): PostReaction =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Добавление реакции к посту",
            additionalData =
                mapOf(
                    "postId" to reaction.postId,
                    "userId" to reaction.userId,
                    "reaction" to reaction.reaction.name))

        try {
          // Сначала удаляем существующую реакцию пользователя
          val deleteBody =
              DbDeleteRequest(
                  table = "post_reactions",
                  condition = "postid = ? AND userid = ?",
                  conditionParams = listOf(reaction.postId, reaction.userId))

          logger.logActivity(
              "Удаление существующей реакции пользователя",
              additionalData = mapOf("postId" to reaction.postId, "userId" to reaction.userId))

          http.delete("$baseUrl/delete") {
            contentType(ContentType.Application.Json)
            setBody(deleteBody)
          }

          // Затем добавляем новую реакцию
          val createBody =
              DbCreateRequest(
                  table = "post_reactions",
                  data =
                      mapOf(
                          "postid" to reaction.postId,
                          "userid" to reaction.userId,
                          "reaction" to reaction.reaction.name))

          val resp: DbResponse =
              http
                  .post("$baseUrl/create") {
                    contentType(ContentType.Application.Json)
                    setBody(createBody)
                  }
                  .body()

          if (resp.success != true) {
            logger.logError(
                "Ошибка при добавлении реакции: postId=${reaction.postId}, userId=${reaction.userId}",
                errorMessage = resp.error ?: "Неизвестная ошибка")
            error("Failed to add reaction: ${resp.error}")
          }

          logger.logActivity(
              "Реакция успешно добавлена",
              additionalData =
                  mapOf(
                      "postId" to reaction.postId,
                      "userId" to reaction.userId,
                      "reaction" to reaction.reaction.name))

          reaction
        } catch (e: Exception) {
          logger.logError(
              "Исключение при добавлении реакции: postId=${reaction.postId}, userId=${reaction.userId}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Удаляет реакцию пользователя с поста
   *
   * @param postId Идентификатор поста
   * @param userId Идентификатор пользователя
   * @return `true`, если удаление прошло успешно
   */
  override suspend fun removeReaction(postId: String, userId: String): Boolean =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Удаление реакции пользователя с поста",
            additionalData = mapOf("postId" to postId, "userId" to userId))

        try {
          val deleteBody =
              DbDeleteRequest(
                  table = "post_reactions",
                  condition = "postid = ? AND userid = ?",
                  conditionParams = listOf(postId, userId))

          val resp: DbResponse =
              http
                  .delete("$baseUrl/delete") {
                    contentType(ContentType.Application.Json)
                    setBody(deleteBody)
                  }
                  .body()

          val success = resp.success == true

          if (success) {
            logger.logActivity(
                "Реакция успешно удалена",
                additionalData = mapOf("postId" to postId, "userId" to userId))
          } else {
            logger.logActivity(
                "Реакция не найдена или не удалена",
                additionalData =
                    mapOf(
                        "postId" to postId,
                        "userId" to userId,
                        "error" to (resp.error ?: "Неизвестная ошибка")))
          }

          success
        } catch (e: Exception) {
          logger.logError(
              "Исключение при удалении реакции: postId=$postId, userId=$userId",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }
}
