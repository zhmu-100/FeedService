package com.mad.feed.plugins

import com.mad.feed.actions.*
import com.mad.feed.services.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

/**
 * Конфигурация внедрения зависимостей для Ktor приложения
 *
 * Устанавливает Koin для управления зависимостями и предоставляет модули для сервисов и действий.
 */
fun Application.configureDependencyInjection() {
  install(Koin) {
    slf4jLogger()
    modules(appModule(this@configureDependencyInjection))
  }
}

/**
 * Модуль приложения для Koin
 *
 * Содержит определения зависимостей для действий и сервисов.
 *
 * @param app экземпляр приложения Ktor
 */
fun appModule(app: Application) = module {
  // Repositories
  single<IPostAction> { PostAction() }
  single<ICommentAction> { CommentAction() }
  single<IReactionAction> { ReactionAction() }

  // Services
  single { PostService(get()) }
  single { CommentService(get(), get()) }
  single { ReactionService(get()) }
}
