package com.mad.feed.services

import com.mad.feed.actions.IPostAction
import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import java.util.UUID

/**
 * Управление постами Бизнес логика обработки постов
 *
 * @property postAction Действия с постами ([IPostAction])
 */
class PostService(private val postAction: IPostAction) {
  private val logger = LoggerProvider.logger

  /**
   * Создание поста
   *
   * @param request Запрос на создание поста ([CreatePostRequest])
   * @return Созданный пост ([Post])
   */
  suspend fun createPost(request: CreatePostRequest): Post {
    logger.logActivity(
        "Создание поста",
        additionalData =
            mapOf(
                "userId" to request.post.userId,
                "attachmentsCount" to request.post.attachments.size.toString()))

    try {
      val inPost = request.post
      val postId = UUID.randomUUID().toString()

      val attachments =
          inPost.attachments.map { a ->
            PostAttachment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                type = a.type,
                position = a.position,
                minioId = a.minioId)
          }

      val post = inPost.copy(id = postId, attachments = attachments)
      val createdPost = postAction.createPost(post)

      logger.logActivity(
          "Пост успешно создан",
          additionalData = mapOf("postId" to postId, "userId" to post.userId))

      return createdPost
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при создании поста: userId=${request.post.userId}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получение поста по ID
   *
   * @param id ID поста ([String])
   * @return Пост ([Post]?) или null, если не найден
   */
  suspend fun getPostById(id: String): Post? {
    logger.logActivity("Получение поста по ID", additionalData = mapOf("postId" to id))

    try {
      val post = postAction.getPostById(id)

      if (post == null) {
        logger.logActivity("Пост не найден", additionalData = mapOf("postId" to id))
      } else {
        logger.logActivity(
            "Пост успешно получен", additionalData = mapOf("postId" to id, "userId" to post.userId))
      }

      return post
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении поста: postId=$id",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получение постов пользователя
   *
   * @param userId id пользователя ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Посты пользователя ([List<Post>])
   */
  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long> {
    logger.logActivity(
        "Получение постов пользователя",
        additionalData =
            mapOf("userId" to userId, "page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val result = postAction.listUserPosts(userId, page, pageSize)

      logger.logActivity(
          "Посты пользователя успешно получены",
          additionalData =
              mapOf(
                  "userId" to userId,
                  "postsCount" to result.first.size.toString(),
                  "totalPosts" to result.second.toString()))

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении постов пользователя: userId=$userId, page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получение постов для общего фида
   *
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Посты ([List<Post>])
   */
  suspend fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long> {
    logger.logActivity(
        "Получение списка постов",
        additionalData = mapOf("page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val result = postAction.listPosts(page, pageSize)

      logger.logActivity(
          "Список постов успешно получен",
          additionalData =
              mapOf(
                  "postsCount" to result.first.size.toString(),
                  "totalPosts" to result.second.toString()))

      return result
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка постов: page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
