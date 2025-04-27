package com.mad.feed.services

import com.mad.feed.actions.ICommentAction
import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.PostComment
import java.util.UUID
import kotlinx.datetime.Instant

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
  /**
   * Создание комментария к посту
   *
   * @param request Запрос на создание комментария ([CreateCommentRequest])
   * @return Созданный комментарий ([PostComment]?) или null, если пост не найден
   */
  suspend fun createComment(request: CreateCommentRequest): PostComment? {
    // Verify post exists
    postAction.getPostById(request.postId) ?: return null

    val inComment = request.comment
    val comment =
        inComment.copy(
            id = UUID.randomUUID().toString(),
            date = Instant.fromEpochMilliseconds(System.currentTimeMillis()))

    return commentAction.createComment(request.postId, comment)
  }

  /**
   * Получение комментариев к посту
   *
   * @param postId ID поста ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Комментарии к посту ([List<PostComment>])
   */
  suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment> =
      commentAction.listComments(postId, page, pageSize)
}
