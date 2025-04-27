package com.mad.feed.plugins

import com.mad.feed.routes.configureCommentRoutes
import com.mad.feed.routes.configurePostRoutes
import com.mad.feed.routes.configureReactionRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
  routing {
    configurePostRoutes()
    configureCommentRoutes()
    configureReactionRoutes()
  }
}
