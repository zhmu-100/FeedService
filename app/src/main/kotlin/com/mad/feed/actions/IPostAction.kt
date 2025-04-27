package com.mad.feed.actions

import com.mad.feed.models.*
import kotlin.Long

/**
 * Интерфейс для работы с постами
 *
 * Определяет операции:
 * - [createPost]
 * - создание нового поста
 * - [getPostById]
 * - получение поста по идентификатору
 * - [listUserPosts]
 * - получение списка постов пользователя с пагинацией
 * - [listPosts]
 * - получение списка постов с пагинацией
 */
interface IPostAction {
  suspend fun createPost(post: Post): Post
  suspend fun getPostById(id: String): Post?
  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long>
  suspend fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long>
}
