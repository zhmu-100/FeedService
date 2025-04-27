package com.mad.feed.services

import com.mad.feed.actions.ICommentAction
import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.PostComment
import java.util.UUID

class CommentService(
    private val commentAction: ICommentAction,
    private val postAction: IPostAction
) {
  suspend fun createComment(postId: String, request: CreateCommentRequest): PostComment? {
    // Verify post exists
    postAction.getPostById(postId) ?: return null

    val comment =
        PostComment(
            id = UUID.randomUUID().toString(), userId = request.userId, content = request.content)

    return commentAction.createComment(postId, comment)
  }

  suspend fun listComments(postId: String, page: Int, pageSize: Int) =
      commentAction.listComments(postId, page, pageSize)
}
