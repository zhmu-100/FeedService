package com.mad.feed.repositories

import com.mad.feed.models.PostComment
import com.mad.feed.models.PostReaction
import com.mad.feed.models.tables.CommentReactions
import com.mad.feed.models.tables.PostComments
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant as JavaInstant

interface CommentRepository {
    fun createComment(postId: String, comment: PostComment): PostComment
    fun listComments(postId: String, page: Int, pageSize: Int): Pair<List<PostComment>, Long>
}

class CommentRepositoryImpl : CommentRepository {
    
    override fun createComment(postId: String, comment: PostComment): PostComment = transaction {
        PostComments.insert {
            it[id] = comment.id
            it[PostComments.postId] = postId
            it[userId] = comment.userId
            it[content] = comment.content
            it[date] = JavaInstant.from(comment.date.toJavaInstant())
        }
        
        comment
    }
    
    override fun listComments(postId: String, page: Int, pageSize: Int): Pair<List<PostComment>, Long> = transaction {
        val totalCount = PostComments.select { PostComments.postId eq postId }.count()
        
        val comments = PostComments.select { PostComments.postId eq postId }
            .orderBy(PostComments.date to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { row ->
                val commentId = row[PostComments.id]
                
                val reactions = CommentReactions.select { CommentReactions.commentId eq commentId }
                    .map { reactionRow ->
                        PostReaction(
                            postId = commentId, // Using commentId as postId for comment reactions
                            userId = reactionRow[CommentReactions.userId],
                            reaction = reactionRow[CommentReactions.reaction]
                        )
                    }
                
                PostComment(
                    id = commentId,
                    userId = row[PostComments.userId],
                    content = row[PostComments.content],
                    date = row[PostComments.date].toKotlinInstant(),
                    reactions = reactions
                )
            }
        
        Pair(comments, totalCount)
    }
}
