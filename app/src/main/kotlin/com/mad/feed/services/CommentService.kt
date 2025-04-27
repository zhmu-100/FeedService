package com.mad.feed.services

import com.mad.feed.actions.ICommentAction
import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.PostComment
import java.util.UUID
import kotlinx.datetime.Instant

class CommentService(
    private val commentAction: ICommentAction,
    private val postAction: IPostAction
) {
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

  suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment> =
      commentAction.listComments(postId, page, pageSize)
}
