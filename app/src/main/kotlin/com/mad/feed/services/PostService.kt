package com.mad.feed.services

import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import java.util.UUID

class PostService(
    private val postAction: IPostAction
) {
  suspend fun createPost(request: CreatePostRequest): Post {
    val postId = UUID.randomUUID().toString()

    val attachments =
        request.attachments.map { attachment ->
          PostAttachment(
              id = UUID.randomUUID().toString(),
              postId = postId,
              type = attachment.type,
              position = attachment.position,
              url = attachment.url)
        }

    val post =
        Post(
            id = postId,
            userId = request.userId,
            content = request.content,
            attachments = attachments)

    return postAction.createPost(post)
  }

  suspend fun getPostById(id: String): Post? = postAction.getPostById(id)

  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int) =
    postAction.listUserPosts(userId, page, pageSize)

  suspend fun listPosts(page: Int, pageSize: Int) =
    postAction.listPosts(page, pageSize)
}
