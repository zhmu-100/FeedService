package com.mad.feed.dto

import kotlinx.serialization.Serializable

data class DbPostRow(
    val id: String,
    val userid: String,
    val content: String? = null,
    val date: String
)

@Serializable
data class DbPostAttachmentRow(
    val id: String,
    val postid: String,
    val type: String,
    val position: Int,
    val minio_id: String
)

@Serializable
data class DbPostCommentRow(
    val id: String,
    val postid: String,
    val userid: String,
    val content: String,
    val date: String
)

@Serializable
data class DbPostReactionRow(val postid: String, val userid: String, val reaction: String)

@Serializable
data class DbCommentReactionRow(val commentid: String, val userid: String, val reaction: String)
