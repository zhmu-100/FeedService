package com.mad.feed.actions

import com.mad.feed.models.PostComment

interface ICommentAction {
  suspend fun createComment(postId: String, comment: PostComment): PostComment
  suspend fun listComments(postId: String, page: Int, pageSize: Int): List<PostComment>
}
