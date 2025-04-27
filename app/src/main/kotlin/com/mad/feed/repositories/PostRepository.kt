package com.mad.feed.repositories

import com.mad.feed.models.*
import com.mad.feed.models.tables.*
import java.time.Instant as JavaInstant
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toKotlinInstant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

interface PostRepository {
  fun createPost(post: Post): Post
  fun getPostById(id: String): Post?
  fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long>
  fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long>
}

class PostRepositoryImpl : PostRepository {

  override fun createPost(post: Post): Post = transaction {
    Posts.insert {
      it[id] = post.id
      it[userId] = post.userId
      it[content] = post.content
      it[date] = JavaInstant.from(post.date.toJavaInstant())
    }

    post.attachments.forEach { attachment ->
      PostAttachments.insert {
        it[id] = attachment.id
        it[postId] = post.id
        it[type] = attachment.type
        it[position] = attachment.position
        it[url] = attachment.url
      }
    }

    post
  }

  override fun getPostById(id: String): Post? = transaction {
    val postRow = Posts.select { Posts.id eq id }.singleOrNull() ?: return@transaction null

    val attachments =
        PostAttachments.select { PostAttachments.postId eq id }
            .map { row ->
              PostAttachment(
                  id = row[PostAttachments.id],
                  postId = row[PostAttachments.postId],
                  type = row[PostAttachments.type],
                  position = row[PostAttachments.position],
                  url = row[PostAttachments.url])
            }

    val reactions =
        PostReactions.select { PostReactions.postId eq id }
            .map { row ->
              PostReaction(
                  postId = row[PostReactions.postId],
                  userId = row[PostReactions.userId],
                  reaction = row[PostReactions.reaction])
            }

    val comments =
        PostComments.select { PostComments.postId eq id }
            .map { row ->
              val commentId = row[PostComments.id]
              val commentReactions =
                  CommentReactions.select { CommentReactions.commentId eq commentId }
                      .map { reactionRow ->
                        PostReaction(
                            postId = commentId, // Using commentId as postId for comment reactions
                            userId = reactionRow[CommentReactions.userId],
                            reaction = reactionRow[CommentReactions.reaction])
                      }

              PostComment(
                  id = commentId,
                  userId = row[PostComments.userId],
                  content = row[PostComments.content],
                  date = row[PostComments.date].toKotlinInstant(),
                  reactions = commentReactions)
            }

    Post(
        id = postRow[Posts.id],
        userId = postRow[Posts.userId],
        content = postRow[Posts.content],
        date = postRow[Posts.date].toKotlinInstant(),
        attachments = attachments,
        reactions = reactions,
        comments = comments)
  }

  override fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long> =
      transaction {
        val totalCount = Posts.select { Posts.userId eq userId }.count()

        val posts =
            Posts.select { Posts.userId eq userId }
                .orderBy(Posts.date to SortOrder.DESC)
                .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
                .map { row ->
                  val postId = row[Posts.id]

                  val attachments =
                      PostAttachments.select { PostAttachments.postId eq postId }
                          .map { attachmentRow ->
                            PostAttachment(
                                id = attachmentRow[PostAttachments.id],
                                postId = attachmentRow[PostAttachments.postId],
                                type = attachmentRow[PostAttachments.type],
                                position = attachmentRow[PostAttachments.position],
                                url = attachmentRow[PostAttachments.url])
                          }

                  val reactions =
                      PostReactions.select { PostReactions.postId eq postId }
                          .map { reactionRow ->
                            PostReaction(
                                postId = reactionRow[PostReactions.postId],
                                userId = reactionRow[PostReactions.userId],
                                reaction = reactionRow[PostReactions.reaction])
                          }

                  Post(
                      id = postId,
                      userId = row[Posts.userId],
                      content = row[Posts.content],
                      date = row[Posts.date].toKotlinInstant(),
                      attachments = attachments,
                      reactions = reactions,
                      comments = emptyList() // Not loading comments for list views
                      )
                }

        Pair(posts, totalCount)
      }

  override fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long> = transaction {
    val totalCount = Posts.selectAll().count()

    val posts =
        Posts.selectAll()
            .orderBy(Posts.date to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { row ->
              val postId = row[Posts.id]

              val attachments =
                  PostAttachments.select { PostAttachments.postId eq postId }
                      .map { attachmentRow ->
                        PostAttachment(
                            id = attachmentRow[PostAttachments.id],
                            postId = attachmentRow[PostAttachments.postId],
                            type = attachmentRow[PostAttachments.type],
                            position = attachmentRow[PostAttachments.position],
                            url = attachmentRow[PostAttachments.url])
                      }

              val reactions =
                  PostReactions.select { PostReactions.postId eq postId }
                      .map { reactionRow ->
                        PostReaction(
                            postId = reactionRow[PostReactions.postId],
                            userId = reactionRow[PostReactions.userId],
                            reaction = reactionRow[PostReactions.reaction])
                      }

              Post(
                  id = postId,
                  userId = row[Posts.userId],
                  content = row[Posts.content],
                  date = row[Posts.date].toKotlinInstant(),
                  attachments = attachments,
                  reactions = reactions,
                  comments = emptyList() // Not loading comments for list views
                  )
            }

    Pair(posts, totalCount)
  }
}
