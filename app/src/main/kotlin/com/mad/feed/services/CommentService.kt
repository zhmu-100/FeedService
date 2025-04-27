package com.mad.feed.services

import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.PostComment
import com.mad.feed.repositories.CommentRepository
import com.mad.feed.repositories.PostRepository
import java.util.UUID

class CommentService(
    private val commentRepository: CommentRepository,
    private val postRepository: PostRepository
) {
  fun createComment(postId: String, request: CreateCommentRequest): PostComment? {
    // Verify post exists
    val post = postRepository.getPostById(postId) ?: return null

    val comment =
        PostComment(
            id = UUID.randomUUID().toString(), userId = request.userId, content = request.content)

    return commentRepository.createComment(postId, comment)
  }

  fun listComments(postId: String, page: Int, pageSize: Int): Pair<List<PostComment>, Long> {
    return commentRepository.listComments(postId, page, pageSize)
  }
}
