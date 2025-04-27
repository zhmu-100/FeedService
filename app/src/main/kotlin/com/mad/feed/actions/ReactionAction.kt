package com.mad.feed.actions

import com.mad.feed.dto.*
import com.mad.feed.models.PostReaction
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Реализация интерфейса [IReactionAction] для работы с реакциями на посты
 *
 * Выполняет HTTP-запросы к сервису базы данных для создания и удаления реакций
 * @param config Конфигурация приложения для определения адреса базы данных
 *
 * Реализует методы:
 * - [addReaction]
 * - [removeReaction]
 */
class ReactionAction(config: ApplicationConfig) : IReactionAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"
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
        val deleteBody =
            DbDeleteRequest(
                table = "post_reactions",
                condition = "postid = ? AND userid = ?",
                conditionParams = listOf(reaction.postId, reaction.userId))
        http.delete("$baseUrl/delete") {
          contentType(ContentType.Application.Json)
          setBody(deleteBody)
        }

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

        if (resp.success != true) error("Failed to add reaction: ${resp.error}")
        reaction
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

        resp.success == true
      }
}
