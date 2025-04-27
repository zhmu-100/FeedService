package com.mad.feed.actions

import com.mad.feed.dto.*
import com.mad.feed.models.PostComment
import com.mad.feed.models.PostReaction
import com.mad.feed.models.ReactionType
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class CommentAction(config: ApplicationConfig) : ICommentAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"
  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"

  private val http = HttpClient { install(ContentNegotiation) { json() } }

  override suspend fun createComment(postId: String, comment: PostComment): PostComment =
      withContext(Dispatchers.IO) {
        val body =
            DbCreateRequest(
                table = "post_comments",
                data =
                    mapOf(
                        "id" to comment.id,
                        "postid" to postId,
                        "userid" to comment.userId,
                        "content" to comment.content,
                        "date" to comment.date.toString()))

        val resp: DbResponse =
            http
                .post("$baseUrl/create") {
                  contentType(ContentType.Application.Json)
                  setBody(body)
                }
                .body()

        if (resp.success != true) {
          error("Failed to create comment: ${resp.error}")
        }
        comment
      }

  override suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment> =
      withContext(Dispatchers.IO) {
        val commentRows =
            callRead<DbPostCommentRow>("post_comments", mapOf("postid" to postId))
                .sortedByDescending { Instant.parse(it.date) }
                .drop((page - 1) * pageSize)
                .take(pageSize)

        commentRows.map { row ->
          val reactionRows =
              callRead<DbCommentReactionRow>("comment_reactions", mapOf("commentid" to row.id))
          PostComment(
              id = row.id,
              userId = row.userid,
              content = row.content,
              date = Instant.parse(row.date),
              reactions =
                  reactionRows.map {
                    PostReaction(row.id, it.userid, ReactionType.valueOf(it.reaction))
                  })
        }
      }

  private suspend inline fun <reified R> callRead(
      table: String,
      filters: Map<String, String>? = null
  ): List<R> =
      http
          .post("$baseUrl/read") {
            contentType(ContentType.Application.Json)
            setBody(DbReadRequest(table = table, filters = filters))
          }
          .body()
}
