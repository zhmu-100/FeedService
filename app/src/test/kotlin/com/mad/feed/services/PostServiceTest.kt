package com.mad.feed.services

import com.mad.feed.actions.IPostAction
import com.mad.feed.models.AttachmentType
import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PostServiceTest {
  private val postAction: IPostAction = mockk()
  private val service = PostService(postAction)

  @Test
  fun `createPost should generate new ids and delegate to postAction`() = runBlocking {
    // Given: original post with one dummy attachment
    val originalAttachment =
        PostAttachment(
            id = "orig-att-id",
            postId = "orig-post-id",
            type = AttachmentType.ATTACHMENT_TYPE_IMAGE,
            position = 1,
            minioId = "minio123")
    val inputPost =
        Post(
            id = "orig-post-id",
            userId = "userA",
            content = "Hello",
            attachments = listOf(originalAttachment))
    val request = CreatePostRequest(post = inputPost)

    // Stub action to echo back its argument
    coEvery { postAction.createPost(any()) } answers { firstArg() }

    // When
    val result = service.createPost(request)

    // Then
    // Should have new post id different from original
    assertNotEquals(inputPost.id, result.id)
    assertEquals(inputPost.userId, result.userId)
    assertEquals(inputPost.content, result.content)
    // Attachments recreated with new ids and correct postId
    assertEquals(1, result.attachments.size)
    val newAtt = result.attachments.first()
    assertNotEquals(originalAttachment.id, newAtt.id)
    assertEquals(result.id, newAtt.postId)
    assertEquals(originalAttachment.type, newAtt.type)
    assertEquals(originalAttachment.position, newAtt.position)
    assertEquals(originalAttachment.minioId, newAtt.minioId)

    coVerify(exactly = 1) { postAction.createPost(result) }
  }

  @Test
  fun `getPostById should delegate and return post`() = runBlocking {
    val postId = "postX"
    val expected = Post(id = postId, userId = "userB")
    coEvery { postAction.getPostById(postId) } returns expected

    val actual = service.getPostById(postId)

    assertEquals(expected, actual)
    coVerify(exactly = 1) { postAction.getPostById(postId) }
  }

  @Test
  fun `getPostById should return null if not found`() = runBlocking {
    val postId = "notFound"
    coEvery { postAction.getPostById(postId) } returns null

    val actual = service.getPostById(postId)

    assertNull(actual)
    coVerify(exactly = 1) { postAction.getPostById(postId) }
  }

  @Test
  fun `listUserPosts should delegate and return pair`() = runBlocking {
    val userId = "userC"
    val page = 2
    val pageSize = 5
    val posts = listOf(Post(id = "1", userId = userId), Post(id = "2", userId = userId))
    val total = 42L
    coEvery { postAction.listUserPosts(userId, page, pageSize) } returns Pair(posts, total)

    val (actualPosts, actualTotal) = service.listUserPosts(userId, page, pageSize)

    assertEquals(posts, actualPosts)
    assertEquals(total, actualTotal)
    coVerify(exactly = 1) { postAction.listUserPosts(userId, page, pageSize) }
  }

  @Test
  fun `listPosts should delegate and return pair`() = runBlocking {
    val page = 1
    val pageSize = 10
    val posts = listOf(Post(id = "A", userId = "u1"))
    val total = 1L
    coEvery { postAction.listPosts(page, pageSize) } returns Pair(posts, total)

    val (actualPosts, actualTotal) = service.listPosts(page, pageSize)

    assertEquals(posts, actualPosts)
    assertEquals(total, actualTotal)
    coVerify(exactly = 1) { postAction.listPosts(page, pageSize) }
  }
}
