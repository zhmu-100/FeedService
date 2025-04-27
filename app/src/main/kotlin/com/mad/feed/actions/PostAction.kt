package com.mad.feed.actions

import com.mad.feed.dto.*
import com.mad.feed.models.*
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

class PostAction(private val config: ApplicationConfig) : IPostAction {

  private val dbMode = config.propertyOrNull("ktor.database.mode")?.getString() ?: "LOCAL"
  private val dbHost = config.propertyOrNull("ktor.database.host")?.getString() ?: "localhost"
  private val dbPort = config.propertyOrNull("ktor.database.port")?.getString() ?: "8080"

  private val baseUrl =
      if (dbMode.equals("gateway", true)) "http://$dbHost:$dbPort/api/db"
      else "http://$dbHost:$dbPort"

  private val http = HttpClient { install(ContentNegotiation) { json() } }

  override suspend fun createPost(post: Post): Post =
      withContext(Dispatchers.IO) {
        callCreate(
            "posts",
            mapOf(
                "id" to post.id,
                "userid" to post.userId,
                "content" to (post.content ?: ""),
                "date" to post.date.toString()))

        post.attachments.forEach { a ->
          callCreate(
              "post_attachments",
              mapOf(
                  "id" to a.id,
                  "postid" to a.postId,
                  "type" to a.type.name,
                  "position" to a.position.toString(),
                  "url" to a.url))
        }

        post
      }

  override suspend fun getPostById(id: String): Post? =
      withContext(Dispatchers.IO) {
        val postRow =
            callRead<DbPostRow>("posts", mapOf("id" to id)).firstOrNull() ?: return@withContext null

        val attachmentsRows =
            callRead<DbPostAttachmentRow>("post_attachments", mapOf("postid" to id))
        val reactionsRows = callRead<DbPostReactionRow>("post_reactions", mapOf("postid" to id))
        val commentsRows = callRead<DbPostCommentRow>("post_comments", mapOf("postid" to id))

        val comments =
            commentsRows.map { cRow ->
              val crRows =
                  callRead<DbCommentReactionRow>("comment_reactions", mapOf("commentid" to cRow.id))
              PostComment(
                  id = cRow.id,
                  userId = cRow.userid,
                  content = cRow.content,
                  date = Instant.parse(cRow.date),
                  reactions =
                      crRows.map { cr ->
                        PostReaction(cRow.id, cr.userid, ReactionType.valueOf(cr.reaction))
                      })
            }

        Post(
            id = postRow.id,
            userId = postRow.userid,
            content = postRow.content.takeIf { it?.isNotBlank() == true },
            date = Instant.parse(postRow.date),
            attachments =
                attachmentsRows.map { a ->
                  PostAttachment(
                      id = a.id,
                      postId = a.postid,
                      type = AttachmentType.valueOf(a.type),
                      position = a.position,
                      url = a.url)
                },
            reactions =
                reactionsRows.map { r ->
                  PostReaction(id, r.userid, ReactionType.valueOf(r.reaction))
                },
            comments = comments)
      }

  override suspend fun listUserPosts(
      userId: String,
      page: Int,
      pageSize: Int
  ): Pair<List<Post>, Long> = listPostsInternal(filters = mapOf("userid" to userId), page, pageSize)

  override suspend fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long> =
      listPostsInternal(filters = null, page, pageSize)

  private suspend fun listPostsInternal(
      filters: Map<String, String>?,
      page: Int,
      pageSize: Int
  ): Pair<List<Post>, Long> =
      withContext(Dispatchers.IO) {
        val postRows =
            callRead<DbPostRow>("posts", filters).sortedByDescending { Instant.parse(it.date) }

        val total = postRows.size.toLong()
        val slice = postRows.drop((page - 1) * pageSize).take(pageSize)

        val posts = slice.map { pr -> getPostById(pr.id)!! }
        Pair(posts, total)
      }

  private suspend fun callCreate(table: String, data: Map<String, String>) {
    val body = DbCreateRequest(table, data)
    val resp: DbResponse =
        http
            .post("$baseUrl/create") {
              contentType(ContentType.Application.Json)
              setBody(body)
            }
            .body()
    if (resp.success != true) error("DB create failed: ${resp.error}")
  }

  private suspend inline fun <reified R> callRead(
      table: String,
      filters: Map<String, String>?
  ): List<R> {
    val body = DbReadRequest(table = table, filters = filters)
    return http
        .post("$baseUrl/read") {
          contentType(ContentType.Application.Json)
          setBody(body)
        }
        .body()
  }
}
