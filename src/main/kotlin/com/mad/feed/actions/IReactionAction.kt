package com.mad.feed.actions

import com.mad.feed.models.PostReaction

/**
 * Интерфейс для работы с реакциями на посты
 *
 * Определяет операции:
 * - [addReaction]
 * - добавление реакции к посту
 * - [removeReaction]
 * - удаление реакции к посту
 */
interface IReactionAction {
  suspend fun addReaction(reaction: PostReaction): PostReaction
  suspend fun removeReaction(postId: String, userId: String): Boolean
}
