package com.mad.feed.actions

import com.mad.feed.models.PostReaction

interface IReactionAction {
  suspend fun addReaction(reaction: PostReaction): PostReaction
  suspend fun removeReaction(postId: String, userId: String): Boolean
}
