package com.mad.feed.plugins

import com.mad.feed.actions.*
import com.mad.feed.services.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
  install(Koin) {
    slf4jLogger()
    modules(appModule(this@configureDependencyInjection))
  }
}

fun appModule(app: Application) = module {
  single { app.environment.config }
  // Repositories
  single<IPostAction> { PostAction(get()) }
  single<ICommentAction> { CommentAction(get()) }
  single<IReactionAction> { ReactionAction(get()) }

  // Services
  single { PostService(get()) }
  single { CommentService(get(), get()) }
  single { ReactionService(get()) }
}
