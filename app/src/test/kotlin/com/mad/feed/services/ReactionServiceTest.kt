package com.mad.feed.services

import com.mad.feed.actions.IReactionAction
import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction
import com.mad.feed.models.ReactionType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReactionServiceTest {
  private val reactionAction: IReactionAction = mockk()
  private val service = ReactionService(reactionAction)

  @Test
  fun `addReaction should delegate to reactionAction and return result`() = runBlocking {
    val postId = "post123"
    val userId = "user456"
    val reactionType = ReactionType.REACTION_LIKE
    val request = AddReactionRequest(postId = postId, userId = userId, reaction = reactionType)
    val expectedReaction = PostReaction(postId = postId, userId = userId, reaction = reactionType)
    coEvery { reactionAction.addReaction(expectedReaction) } returns expectedReaction

    val actual = service.addReaction(postId, request)

    assertEquals(expectedReaction, actual)
    coVerify(exactly = 1) { reactionAction.addReaction(expectedReaction) }
  }

  @Test
  fun `removeReaction should delegate to reactionAction and return true when successful`() =
      runBlocking {
        val postId = "post123"
        val userId = "user456"
        coEvery { reactionAction.removeReaction(postId, userId) } returns true

        val result = service.removeReaction(postId, userId)

        assertTrue(result)
        coVerify(exactly = 1) { reactionAction.removeReaction(postId, userId) }
      }

  @Test
  fun `removeReaction should delegate to reactionAction and return false when unsuccessful`() =
      runBlocking {
        val postId = "post123"
        val userId = "user456"
        coEvery { reactionAction.removeReaction(postId, userId) } returns false

        val result = service.removeReaction(postId, userId)

        assertFalse(result)
        coVerify(exactly = 1) { reactionAction.removeReaction(postId, userId) }
      }
}
