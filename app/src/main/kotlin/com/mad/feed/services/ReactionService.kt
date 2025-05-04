package com.mad.feed.services

import com.mad.feed.actions.IReactionAction
import com.mad.feed.logging.LoggerProvider
import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction

/**
 * Управление реакциями Бизнес логика обработки реакций
 *
 * @property reactionAction Действия с реакциями ([IReactionAction])
 */
class ReactionService(private val reactionAction: IReactionAction) {
  private val logger = LoggerProvider.logger

  /**
   * Добавление реакции к посту
   *
   * @param postId ID поста ([String])
   * @param request Запрос на добавление реакции ([AddReactionRequest])
   * @return Реакция на пост ([PostReaction])
   */
  suspend fun addReaction(postId: String, request: AddReactionRequest): PostReaction {
    logger.logActivity(
        "Добавление реакции к посту",
        additionalData =
            mapOf(
                "postId" to postId,
                "userId" to request.userId,
                "reaction" to request.reaction.name))

    try {
      val reaction =
          reactionAction.addReaction(
              PostReaction(postId = postId, userId = request.userId, reaction = request.reaction))

      logger.logActivity(
          "Реакция успешно добавлена",
          additionalData =
              mapOf(
                  "postId" to postId,
                  "userId" to request.userId,
                  "reaction" to request.reaction.name))

      return reaction
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при добавлении реакции: postId=$postId, userId=${request.userId}",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }

  /**
   * Получение реакций на пост
   *
   * @param postId ID поста ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Реакции на пост ([List<PostReaction>])
   */
  suspend fun removeReaction(postId: String, userId: String): Boolean {
    logger.logActivity(
        "Удаление реакции пользователя с поста",
        additionalData = mapOf("postId" to postId, "userId" to userId))

    try {
      val success = reactionAction.removeReaction(postId, userId)

      if (success) {
        logger.logActivity(
            "Реакция успешно удалена",
            additionalData = mapOf("postId" to postId, "userId" to userId))
      } else {
        logger.logActivity(
            "Реакция не найдена", additionalData = mapOf("postId" to postId, "userId" to userId))
      }

      return success
    } catch (e: Exception) {
      logger.logError(
          "Ошибка при удалении реакции: postId=$postId, userId=$userId",
          errorMessage = e.message ?: "Неизвестная ошибка",
          stackTrace = e.stackTraceToString())
      throw e
    }
  }
}
