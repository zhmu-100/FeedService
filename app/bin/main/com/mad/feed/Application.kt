package com.mad.feed

import com.mad.feed.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/**
 * Точка входа в фид сервис.
 *
 * Сервис запускается на порту 8082 либо по application.conf, хост 0.0.0.0
 */
fun main() {
  embeddedServer(
          Netty,
          port = System.getenv("PORT")?.toIntOrNull() ?: 8082,
          host = "0.0.0.0",
          module = Application::module)
      .start(wait = true)
}

/**
 * Подключает:
 * - сериализацию [configureSerialization]
 * - мониторинг [configureMonitoring]
 * - мзависимости [configureDependencyInjection]
 * - маршрутизацию [configureRouting]
 */
fun Application.module() {
  configureSerialization()
  configureMonitoring()
  configureDependencyInjection()
  configureRouting()
}
