package com.mad.feed.actions

import com.mad.feed.models.*
import kotlin.Long

interface IPostAction {
  suspend fun createPost(post: Post): Post
  suspend fun getPostById(id: String): Post?
  suspend fun listUserPosts(userId: String, page: Int, pageSize: Int): Pair<List<Post>, Long>
  suspend fun listPosts(page: Int, pageSize: Int): Pair<List<Post>, Long>
}
