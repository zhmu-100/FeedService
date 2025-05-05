package com.mad.feed.actions

import com.mad.feed.dto.*
import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

/**
 * Реализация интерфейса [IPostAction] для работы с постами
 *
 * Реализует методы:
 * - [createPost]
 * - [getPostById]
 * - [listUserPosts]
 * - [listPosts]
 */
class PostAction : IPostAction {
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
   * Создает новый пост и его вложения
   *
   * @param post Пост для создания
   * @return Созданный пост
   */
  override suspend fun createPost(post: Post): Post =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Создание нового поста",
            additionalData =
                mapOf(
                    "postId" to post.id,
                    "userId" to post.userId,
                    "attachmentsCount" to post.attachments.size.toString()))

        try {
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
                    "minio_id" to a.minioId))
          }

          logger.logActivity(
              "Пост успешно создан",
              additionalData = mapOf("postId" to post.id, "userId" to post.userId))

          post
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при создании поста: postId=${post.id}, userId=${post.userId}",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Получает пост по его идентификатору вместе с вложениями, реакциями и комментариями
   *
   * @param id Идентификатор поста
   * @return Пост или `null`, если не найден
   */
  override suspend fun getPostById(id: String): Post? =
      withContext(Dispatchers.IO) {
        logger.logActivity("Получение поста по ID", additionalData = mapOf("postId" to id))

        try {
          val postRow = callRead<DbPostRow>("posts", mapOf("id" to id)).firstOrNull()

          if (postRow == null) {
            logger.logActivity("Пост не найден", additionalData = mapOf("postId" to id))
            return@withContext null
          }

          val attachmentsRows =
              callRead<DbPostAttachmentRow>("post_attachments", mapOf("postid" to id))
          val reactionsRows = callRead<DbPostReactionRow>("post_reactions", mapOf("postid" to id))
          val commentsRows = callRead<DbPostCommentRow>("post_comments", mapOf("postid" to id))

          val comments =
              commentsRows.map { cRow ->
                val crRows =
                    callRead<DbCommentReactionRow>(
                        "comment_reactions", mapOf("commentid" to cRow.id))
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

          val post =
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
                            minioId = a.minio_id)
                      },
                  reactions =
                      reactionsRows.map { r ->
                        PostReaction(id, r.userid, ReactionType.valueOf(r.reaction))
                      },
                  comments = comments)

          logger.logActivity(
              "Пост успешно получен",
              additionalData =
                  mapOf(
                      "postId" to id,
                      "attachmentsCount" to post.attachments.size.toString(),
                      "reactionsCount" to post.reactions.size.toString(),
                      "commentsCount" to post.comments.size.toString()))

          post
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении поста: postId=$id",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  override suspend fun listUserPosts(
      userId: String,
      page: Int,
      pageSize: Int
  ): Pair<List<Post>, Long> = listPostsInternal(filters = mapOf("userid" to userId), page, pageSize)

  /**
   * Получает список постов конкретного пользователя с пагинацией
   * @param page Номер страницы
   * @param pageSize Размер страницы
   * @return Пара из списка постов и общего количества постов
   */
  override suspend fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long> =
      listPostsInternal(filters = null, page, pageSize)

  /**
   * Получает список постов с пагинацией
   *
   * @param filters Фильтры для выборки постов
   * @param page Номер страницы
   * @param pageSize Размер страницы
   * @return Пара из списка постов и общего количества постов
   */
  private suspend fun listPostsInternal(
      filters: Map<String, String>?,
      page: Int,
      pageSize: Int
  ): Pair<List<Post>, Long> =
      withContext(Dispatchers.IO) {
        logger.logActivity(
            "Получение списка постов",
            additionalData =
                mapOf(
                    "filters" to (filters?.toString() ?: "null"),
                    "page" to page.toString(),
                    "pageSize" to pageSize.toString()))

        try {
          val postRows =
              callRead<DbPostRow>("posts", filters).sortedByDescending { Instant.parse(it.date) }

          val total = postRows.size.toLong()
          val slice = postRows.drop((page - 1) * pageSize).take(pageSize)

          val posts = slice.map { pr -> getPostById(pr.id)!! }

          logger.logActivity(
              "Список постов успешно получен",
              additionalData =
                  mapOf("totalPosts" to total.toString(), "returnedPosts" to posts.size.toString()))

          Pair(posts, total)
        } catch (e: Exception) {
          logger.logError(
              "Ошибка при получении списка постов: filters=${filters?.toString() ?: "null"}, page=$page, pageSize=$pageSize",
              errorMessage = e.message ?: "Неизвестная ошибка",
              stackTrace = e.stackTraceToString())
          throw e
        }
      }

  /**
   * Создает новую запись в базе данных
   *
   * @param table Имя таблицы
   * @param data Данные для вставки в виде карты ключ–значение
   * @throws Exception Если создание записи не удалось
   */
  private suspend fun callCreate(table: String, data: Map<String, String>) {
    logger.logActivity(
        "Запрос к БД: создание записи",
        additionalData = mapOf("table" to table, "dataKeys" to data.keys.joinToString(",")))

    try {
      val body = DbCreateRequest(table, data)
      val resp: DbResponse =
          http
              .post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(body)
              }
              .body()

      if (resp.success != true) {
        logger.logError(
            "Ошибка при создании записи в БД: table=$table",
            errorMessage = resp.error ?: "Неизвестная ошибка")
        error("DB create failed: ${resp.error}")
      }

      logger.logActivity("Запись в БД успешно создана", additionalData = mapOf("table" to table))
    } catch (e: Exception) {
      logger.logError(
          "Исключение при создании записи в БД: table=$table",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Выполняет SELECT запрос для получения записей из таблицы
   *
   * @param table Имя таблицы
   * @param filters Карта фильтров для условия WHERE
   * @return Список записей в виде карты ключ–значение
   */
  private suspend inline fun <reified R> callRead(
      table: String,
      filters: Map<String, String>? = null
  ): List<R> {
    logger.logActivity(
        "Запрос к БД: чтение данных",
        additionalData = mapOf("table" to table, "filters" to (filters?.toString() ?: "null")))

    try {
      val result =
          http
              .post("$baseUrl/read") {
                contentType(ContentType.Application.Json)
                setBody(DbReadRequest(table = table, filters = filters))
              }
              .body<List<R>>()

      logger.logActivity(
          "Данные из БД получены успешно",
          additionalData = mapOf("table" to table, "rowsCount" to result.size.toString()))

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при чтении данных из БД: table=$table, filters=${filters?.toString() ?: "null"}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
