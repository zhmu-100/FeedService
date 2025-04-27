package com.mad.feed.plugins

import com.mad.feed.routes.configureCommentRoutes
import com.mad.feed.routes.configurePostRoutes
import com.mad.feed.routes.configureReactionRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

/**
 * Конфигурация маршрутов для Ktor приложения
 *
 * Устанавливает маршруты для работы с постами, комментариями и реакциями
 */
fun Application.configureRouting() {
  routing {
    configurePostRoutes()
    configureCommentRoutes()
    configureReactionRoutes()
  }
}
