package com.mad.feed.services

import com.mad.feed.actions.IReactionAction
import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction

class ReactionService(private val reactionAction: IReactionAction) {
  suspend fun addReaction(postId: String, request: AddReactionRequest): PostReaction =
    reactionAction.addReaction(
      PostReaction(
        postId = postId,
        userId = request.userId,
        reaction = request.reaction
      )
    )

  suspend fun removeReaction(postId: String, userId: String): Boolean =
    reactionAction.removeReaction(postId, userId)
}
