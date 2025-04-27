package com.mad.feed.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.request.*
import org.slf4j.event.Level

/**
 * Конфигурация мониторинга для Ktor приложения
 *
 * Устанавливает плагин [CallLogging] для логирования входящих запросов и их параметров.
 */
fun Application.configureMonitoring() {
  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }
}
