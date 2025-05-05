package com.mad.feed.actions

import com.mad.feed.models.PostComment

/**
 * Интерфейс для работы с комментариями к постам
 *
 * Определяет операции:
 * - [createComment]
 * - создание нового комментария к посту
 * - [listComments]
 * - получение списка комментариев к посту с пагинацией
 */
interface ICommentAction {
  suspend fun createComment(postId: String, comment: PostComment): PostComment
  suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment>
}
