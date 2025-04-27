package com.mad.feed.plugins

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

/**
 * Конфигурация сериализации JSON для Ktor приложения
 *
 * Устанавливает плагин [ContentNegotiation] с использованием [Json] для обработки JSON данных
 * Настройки:
 * - prettyPrint: форматирование JSON для удобства чтения
 * - isLenient: разрешает более свободный синтаксис JSON
 * - ignoreUnknownKeys: игнорирует неизвестные ключи в JSON
 */
fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(
        Json {
          prettyPrint = true
          isLenient = true
          ignoreUnknownKeys = true
        })
  }
}
