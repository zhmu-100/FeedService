package com.mad.feed.models

import java.util.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String? = null,
    val attachments: List<PostAttachment> = emptyList(),
    val date: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val reactions: List<PostReaction> = emptyList(),
    val comments: List<PostComment> = emptyList()
)

@Serializable
data class PostAttachment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val type: AttachmentType,
    val position: Int,
    val url: String
)

@Serializable
data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String,
    val date: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val reactions: List<PostReaction> = emptyList()
)

@Serializable
data class PostReaction(val postId: String, val userId: String, val reaction: ReactionType)

// Request/Response models
@Serializable
data class CreatePostRequest(
    val userId: String,
    val content: String? = null,
    val attachments: List<PostAttachment> = emptyList()
)

@Serializable data class CreateCommentRequest(val userId: String, val content: String)

@Serializable data class AddReactionRequest(val userId: String, val reaction: ReactionType)

@Serializable data class RemoveReactionRequest(val userId: String)

@Serializable data class PaginationRequest(val page: Int = 1, val pageSize: Int = 20)

@Serializable
data class ListPostsResponse(
    val posts: List<Post>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class ListCommentsResponse(
    val comments: List<PostComment>,
    val totalCount: Long,
    val page: Int,
    val pageSize: Int
)
