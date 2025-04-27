package com.mad.feed.services

import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.PostReaction
import com.mad.feed.repositories.ReactionRepository

class ReactionService(
    private val reactionRepository: ReactionRepository
) {
    fun addReaction(postId: String, request: AddReactionRequest): PostReaction {
        val reaction = PostReaction(
            postId = postId,
            userId = request.userId,
            reaction = request.reaction
        )
        
        return reactionRepository.addReaction(reaction)
    }
    
    fun removeReaction(postId: String, userId: String): Boolean {
        return reactionRepository.removeReaction(postId, userId)
    }
}
