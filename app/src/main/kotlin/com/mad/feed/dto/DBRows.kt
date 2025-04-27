package com.mad.feed.dto

import kotlinx.serialization.Serializable

/**
 * Строка в таблице постов в бд
 *
 * @property id Идентификатор поста
 * @property userid Идентификатор пользователя
 * @property content Содержимое поста
 * @property date Дата создания поста
 */
@Serializable
data class DbPostRow(
    val id: String,
    val userid: String,
    val content: String? = null,
    val date: String
)

/**
 * Строка в таблице вложений постов в бд
 *
 * @property id Идентификатор вложения
 * @property postid Идентификатор поста
 * @property type Тип вложения
 * @property position Позиция вложения в посте
 * @property minio_id Идентификатор вложения в MinIO
 */
@Serializable
data class DbPostAttachmentRow(
    val id: String,
    val postid: String,
    val type: String,
    val position: Int,
    val minio_id: String
)

/**
 * Строка в таблице комментариев к постам в бд
 *
 * @property id Идентификатор комментария
 * @property postid Идентификатор поста
 * @property userid Идентификатор пользователя
 * @property content Содержимое комментария
 * @property date Дата создания комментария
 */
@Serializable
data class DbPostCommentRow(
    val id: String,
    val postid: String,
    val userid: String,
    val content: String,
    val date: String
)

/**
 * Строка в таблице реакций на посты в бд
 *
 * @property postid Идентификатор поста
 * @property userid Идентификатор пользователя
 * @property reaction Реакция
 */
@Serializable
data class DbPostReactionRow(val postid: String, val userid: String, val reaction: String)

/**
 * Строка в таблице реакций на комментарии в бд
 *
 * @property commentid Идентификатор комментария
 * @property userid Идентификатор пользователя
 * @property reaction Реакция
 */
@Serializable
data class DbCommentReactionRow(val commentid: String, val userid: String, val reaction: String)
