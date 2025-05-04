package com.mad.feed.services

import com.mad.feed.actions.ICommentAction
import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostComment
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CommentServiceTest {
  private val commentAction: ICommentAction = mockk()
  private val postAction: IPostAction = mockk()
  private val service = CommentService(commentAction, postAction)

  @Test
  fun `createComment returns null when post not found`() = runBlocking {
    // Given
    val request =
        CreateCommentRequest(
            postId = "missing",
            comment =
                PostComment(
                    id = "c1",
                    userId = "u1",
                    content = "hi",
                    date = Instant.fromEpochMilliseconds(0)))
    coEvery { postAction.getPostById(request.postId) } returns null

    // When
    val result = service.createComment(request)

    // Then
    assertNull(result)
    coVerify(exactly = 1) { postAction.getPostById(request.postId) }
    coVerify(exactly = 0) { commentAction.createComment(any(), any()) }
  }

  @Test
  fun `createComment generates new id and timestamp and delegates to commentAction`() =
      runBlocking {
        // Given existing post
        val postId = "post123"
        val existingPost = Post(id = postId, userId = "u1")
        coEvery { postAction.getPostById(postId) } returns existingPost

        val originalComment =
            PostComment(
                id = "orig-c",
                userId = "u2",
                content = "text",
                date = Instant.fromEpochMilliseconds(0))
        val request = CreateCommentRequest(postId = postId, comment = originalComment)
        // Stub commentAction to return its argument
        coEvery { commentAction.createComment(postId, any()) } answers { secondArg() }

        // When
        val result = service.createComment(request)

        // Then
        assertNotNull(result)
        assertNotEquals(originalComment.id, result!!.id)
        assertEquals(originalComment.userId, result.userId)
        assertEquals(originalComment.content, result.content)
        // date should be recent (greater than original timestamp)
        assertTrue(result.date > originalComment.date)

        coVerify(exactly = 1) { postAction.getPostById(postId) }
        coVerify(exactly = 1) { commentAction.createComment(postId, result) }
      }

  @Test
  fun `listComments should delegate and return list`() = runBlocking {
    // Given
    val postId = "post45"
    val page = 2
    val pageSize = 3
    val comments =
        listOf(
            PostComment(
                id = "c1", userId = "u1", content = "a", date = Instant.fromEpochMilliseconds(1)),
            PostComment(
                id = "c2", userId = "u2", content = "b", date = Instant.fromEpochMilliseconds(2)))
    coEvery { commentAction.listComments(postId, page, pageSize) } returns comments

    // When
    val actual = service.listComments(postId, page, pageSize)

    // Then
    assertEquals(comments, actual)
    coVerify(exactly = 1) { commentAction.listComments(postId, page, pageSize) }
  }
}
