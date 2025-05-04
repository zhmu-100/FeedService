package com.mad.feed

import com.mad.feed.logging.LoggerProvider
import com.mad.feed.plugins.*
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

/** Точка входа в фид сервис. */
fun main() {
  val logger = LoggerProvider.logger
  val dotenv = dotenv()
  val port = dotenv["PORT"]?.toIntOrNull() ?: 8082

  logger.logActivity(
      "Запуск фид сервиса", additionalData = mapOf("port" to port.toString(), "host" to "0.0.0.0"))

  try {
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)

    logger.logActivity("Фид сервис успешно запущен")
  } catch (e: Exception) {
    logger.logError(
        "Ошибка при запуске фид сервиса",
        errorMessage = e.message ?: "Неизвестная ошибка",
        stackTrace = e.stackTraceToString())
    throw e
  }
}

/**
 * Подключает:
 * - сериализацию [configureSerialization]
 * - мониторинг [configureMonitoring]
 * - мзависимости [configureDependencyInjection]
 * - маршрутизацию [configureRouting]
 */
fun Application.module() {
  val logger = LoggerProvider.logger

  logger.logActivity("Настройка модулей приложения")

  try {
    configureSerialization()
    configureMonitoring()
    configureDependencyInjection()
    configureRouting()

    environment.monitor.subscribe(ApplicationStopped) {
      logger.logActivity("Остановка фид сервиса")
      logger.close()
    }

    logger.logActivity("Модули приложения успешно настроены")
  } catch (e: Exception) {
    logger.logError(
        "Ошибка при настройке модулей приложения",
        errorMessage = e.message ?: "Неизвестная ошибка",
        stackTrace = e.stackTraceToString())
    throw e
  }
}
