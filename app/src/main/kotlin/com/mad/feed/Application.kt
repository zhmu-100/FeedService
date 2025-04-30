package com.mad.feed

import com.mad.feed.plugins.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/** Точка входа в фид сервис. */
fun main() {
  val dotenv = dotenv()
  val port = dotenv["PORT"]?.toIntOrNull() ?: 8082
  embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
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
