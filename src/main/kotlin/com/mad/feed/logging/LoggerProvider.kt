package com.mad.feed.logging

import com.mad.client.LoggerClient
import io.github.cdimascio.dotenv.dotenv

/**
 * Провайдер для доступа к логгеру.
 *
 * Инициализирует и предоставляет единый экземпляр [LoggerClient] для всего приложения.
 */
object LoggerProvider {
  private val dotenv = io.github.cdimascio.dotenv.Dotenv.configure().directory(".").load()

  // Инициализация клиента логгера с параметрами из .env файла
  val logger: LoggerClient by lazy {
    LoggerClient(
        host = dotenv["REDIS_HOST"] ?: "localhost",
        port = dotenv["REDIS_PORT"]?.toIntOrNull() ?: 6379,
        password = dotenv["REDIS_PASSWORD"] ?: "",
        activityChannel = dotenv["LOGGER_ACTIVITY_CHANNEL"] ?: "logger:activity",
        errorChannel = dotenv["LOGGER_ERROR_CHANNEL"] ?: "logger:error")
  }
}
