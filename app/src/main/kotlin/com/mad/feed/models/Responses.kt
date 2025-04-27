package com.mad.feed.models

import kotlinx.serialization.Serializable

@Serializable
data class CreatePostRequest(val post: Post)

@Serializable
data class CreateCommentRequest(val postId: String, val comment: PostComment)

@Serializable
data class AddReactionRequest(val postId: String, val userId: String, val reaction: ReactionType)

@Serializable
data class RemoveReactionRequest(val postId: String, val userId: String)

@Serializable
data class PaginationRequest(val page: Int = 1, val pageSize: Int = 20)

@Serializable
data class ListPostsResponse(val posts: List<Post>)

@Serializable
data class ListCommentsResponse(val comments: List<PostComment>)
