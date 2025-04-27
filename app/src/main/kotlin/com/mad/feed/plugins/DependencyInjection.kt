package com.mad.feed.plugins

import com.mad.feed.repositories.*
import com.mad.feed.services.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
  install(Koin) {
    slf4jLogger()
    modules(appModule)
  }
}

val appModule = module {
  // Repositories
  single<PostRepository> { PostRepositoryImpl() }
  single<CommentRepository> { CommentRepositoryImpl() }
  single<ReactionRepository> { ReactionRepositoryImpl() }

  // Services
  single { PostService(get(), get()) }
  single { CommentService(get(), get()) }
  single { ReactionService(get()) }
}
