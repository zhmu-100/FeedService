package com.mad.feed.services

import com.mad.feed.actions.ICommentAction
import com.mad.feed.actions.IPostAction
import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.PostComment
import java.util.UUID
import kotlinx.datetime.Clock

/**
 * Управление комментариями Бизнес логика обработки комментариев
 *
 * @property commentAction Действия с комментариями ([ICommentAction])
 * @property postAction Действия с постами ([IPostAction])
 */
class CommentService(
    private val commentAction: ICommentAction,
    private val postAction: IPostAction
) {
  private val logger = LoggerProvider.logger

  /**
   * Создание комментария к посту
   *
   * @param request Запрос на создание комментария ([CreateCommentRequest])
   * @return Созданный комментарий ([PostComment]?) или null, если пост не найден
   */
  suspend fun createComment(request: CreateCommentRequest): PostComment? {
    logger.logActivity(
        "Создание комментария",
        additionalData = mapOf("postId" to request.postId, "userId" to request.comment.userId))

    try {
      // Проверяем существование поста
      val post = postAction.getPostById(request.postId)

      if (post == null) {
        logger.logActivity(
            "Пост не найден при создании комментария",
            additionalData = mapOf("postId" to request.postId))
        return null
      }

      val commentId = UUID.randomUUID().toString()
      val now = Clock.System.now()

      val comment = request.comment.copy(id = commentId, date = now, reactions = emptyList())

      val createdComment = commentAction.createComment(request.postId, comment)

      logger.logActivity(
          "Комментарий успешно создан",
          additionalData =
              mapOf(
                  "postId" to request.postId, "commentId" to commentId, "userId" to comment.userId))

      return createdComment
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при создании комментария: postId=${request.postId}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получение комментариев к посту
   *
   * @param postId ID поста ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Комментарии к посту ([List<PostComment>])
   */
  suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment> {
    logger.logActivity(
        "Получение списка комментариев",
        additionalData =
            mapOf("postId" to postId, "page" to page.toString(), "pageSize" to pageSize.toString()))

    try {
      val comments = commentAction.listComments(postId, page, pageSize)

      logger.logActivity(
          "Список комментариев получен успешно",
          additionalData = mapOf("postId" to postId, "commentsCount" to comments.size.toString()))

      return comments
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при получении списка комментариев: postId=$postId, page=$page, pageSize=$pageSize",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
