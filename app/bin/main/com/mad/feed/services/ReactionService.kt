package com.mad.feed.services

import com.mad.feed.actions.IReactionAction
import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction

/**
 * Управление реакциями Бизнес логика обработки реакций
 *
 * @property reactionAction Действия с реакциями ([IReactionAction])
 */
class ReactionService(private val reactionAction: IReactionAction) {
  /**
   * Добавление реакции к посту
   *
   * @param postId ID поста ([String])
   * @param request Запрос на добавление реакции ([AddReactionRequest])
   * @return Реакция на пост ([PostReaction])
   */
  suspend fun addReaction(postId: String, request: AddReactionRequest): PostReaction =
      reactionAction.addReaction(
          PostReaction(postId = postId, userId = request.userId, reaction = request.reaction))

  /**
   * Получение реакций на пост
   *
   * @param postId ID поста ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Реакции на пост ([List<PostReaction>])
   */
  suspend fun removeReaction(postId: String, userId: String): Boolean =
      reactionAction.removeReaction(postId, userId)
}
