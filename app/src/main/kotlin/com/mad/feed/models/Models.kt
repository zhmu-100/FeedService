package com.mad.feed.models

import java.util.UUID
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

/**
 * Модель поста
 *
 * @property id Идентификатор поста
 * @property userId Идентификатор пользователя, создавшего пост
 * @property content Содержимое поста
 * @property attachments Список вложений в посте
 * @property date Дата создания поста
 * @property reactions Список реакций на пост
 * @property comments Список комментариев к посту
 */
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

/**
 * Модель вложения в пост
 *
 * @property id Идентификатор вложения
 * @property postId Идентификатор поста, к которому относится вложение
 * @property type Тип вложения
 * @property position Позиция вложения в посте
 * @property minioId Идентификатор вложения в MinIO
 */
@Serializable
data class PostAttachment(
    val id: String = UUID.randomUUID().toString(),
    val postId: String,
    val type: AttachmentType,
    val position: Int,
    val minioId: String
)

/**
 * Модель комментария к посту
 *
 * @property id Идентификатор комментария
 * @property userId Идентификатор пользователя, создавшего комментарий
 * @property content Содержимое комментария
 * @property date Дата создания комментария
 * @property reactions Список реакций на комментарий
 */
@Serializable
data class PostComment(
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val content: String,
    val date: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis()),
    val reactions: List<PostReaction> = emptyList()
)

/**
 * Модель типа вложения
 *
 * @property type Тип вложения
 */
@Serializable
data class PostReaction(val postId: String, val userId: String, val reaction: ReactionType)
