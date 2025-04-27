package com.mad.feed.repositories

import com.mad.feed.models.PostReaction
import com.mad.feed.models.tables.PostReactions
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

interface ReactionRepository {
  fun addReaction(reaction: PostReaction): PostReaction
  fun removeReaction(postId: String, userId: String): Boolean
}

class ReactionRepositoryImpl : ReactionRepository {

  override fun addReaction(reaction: PostReaction): PostReaction = transaction {
    // First remove any existing reaction from this user on this post
    PostReactions.deleteWhere {
      (PostReactions.postId eq reaction.postId) and (PostReactions.userId eq reaction.userId)
    }

    // Then add the new reaction
    PostReactions.insert {
      it[postId] = reaction.postId
      it[userId] = reaction.userId
      it[PostReactions.reaction] = reaction.reaction
    }

    reaction
  }

  override fun removeReaction(postId: String, userId: String): Boolean = transaction {
    val deletedCount =
        PostReactions.deleteWhere {
          (PostReactions.postId eq postId) and (PostReactions.userId eq userId)
        }

    deletedCount > 0
  }
}
