package com.mad.feed.services

import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import com.mad.feed.repositories.PostRepository
import com.mad.feed.repositories.ReactionRepository
import java.util.UUID

class PostService(
    private val postRepository: PostRepository,
    private val reactionRepository: ReactionRepository
) {
  fun createPost(request: CreatePostRequest): Post {
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

    return postRepository.createPost(post)
  }

  fun getPostById(id: String): Post? {
    return postRepository.getPostById(id)
  }

  fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long> {
    return postRepository.listUserPosts(userId, page, pageSize)
  }

  fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long> {
    return postRepository.listPosts(page, pageSize)
  }
}
