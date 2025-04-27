package com.mad.feed.services

import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import java.util.UUID

class PostService(private val postAction: IPostAction) {
  suspend fun createPost(request: CreatePostRequest): Post {
    val inPost = request.post
    val postId = UUID.randomUUID().toString()

    val attachments =
        inPost.attachments.map { a ->
          PostAttachment(
              id = UUID.randomUUID().toString(),
              postId = postId,
              type = a.type,
              position = a.position,
              minioId = a.minioId)
        }

    val post = inPost.copy(id = postId, attachments = attachments)
    return postAction.createPost(post)
  }

  suspend fun getPostById(id: String): Post? = postAction.getPostById(id)

  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int) =
      postAction.listUserPosts(userId, page, pageSize)

  suspend fun listPosts(page: Int, pageSize: Int) = postAction.listPosts(page, pageSize)
}
