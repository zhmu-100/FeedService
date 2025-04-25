package com.mad.feed.models.tables

import com.mad.feed.models.AttachmentType
import com.mad.feed.models.ReactionType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
 
object Posts : Table() {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val userId = varchar("user_id", 36)
    val content = text("content").nullable()
    val date = timestamp("date")
}

object PostAttachments : Table() {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val postId = varchar("post_id", 36).references(Posts.id)
    val type = enumeration<AttachmentType>("type")
    val position = integer("position")
    val url = varchar("url", 255)
}

object PostComments : Table() {
    val id = varchar("id", 36)
    override val primaryKey = PrimaryKey(id)
    val postId = varchar("post_id", 36).references(Posts.id)
    val userId = varchar("user_id", 36)
    val content = text("content")
    val date = timestamp("date")
}

object PostReactions : Table() {
    val postId = varchar("post_id", 36).references(Posts.id)
    val userId = varchar("user_id", 36)
    val reaction = enumeration<ReactionType>("reaction")
    
    override val primaryKey = PrimaryKey(postId, userId)
}

object CommentReactions : Table() {
    val commentId = varchar("comment_id", 36).references(PostComments.id)
    val userId = varchar("user_id", 36)
    val reaction = enumeration<ReactionType>("reaction")
    
    override val primaryKey = PrimaryKey(commentId, userId)
}
