package com.mad.feed.services

import com.mad.feed.actions.IPostAction
import com.mad.feed.models.CreatePostRequest
import com.mad.feed.models.Post
import com.mad.feed.models.PostAttachment
import java.util.UUID

/**
 * Управление постами Бизнес логика обработки постов
 *
 * @property postAction Действия с постами ([IPostAction])
 */
class PostService(private val postAction: IPostAction) {
  /**
   * Создание поста
   *
   * @param request Запрос на создание поста ([CreatePostRequest])
   * @return Созданный пост ([Post])
   */
  suspend fun createPost(request: CreatePostRequest): Post {
    val inPost = request.post
    val postId = UUID.randomUUID().toString()

    val attachments =
        inPost.attachments.map { a ->
          PostAttachment(
              id = UUID.randomUUID().toString(),
              postId = postId,
              type = a.type,
              position = a.position,
              minioId = a.minioId)
        }

    val post = inPost.copy(id = postId, attachments = attachments)
    return postAction.createPost(post)
  }

  /**
   * Получение поста по ID
   *
   * @param id ID поста ([String])
   * @return Пост ([Post]?) или null, если не найден
   */
  suspend fun getPostById(id: String): Post? = postAction.getPostById(id)

  /**
   * Получение постов пользователя
   *
   * @param userId id пользователя ([String])
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Посты пользователя ([List<Post>])
   */
  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int) =
      postAction.listUserPosts(userId, page, pageSize)

  /**
   * Получение постов для общего фида
   *
   * @param page Номер страницы ([Int])
   * @param pageSize Размер страницы ([Int])
   * @return Посты ([List<Post>])
   */
  suspend fun listPosts(page: Int, pageSize: Int) = postAction.listPosts(page, pageSize)
}
