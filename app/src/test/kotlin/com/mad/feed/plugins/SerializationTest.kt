package com.mad.feed.plugins

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SerializationTest {

  @Serializable private data class TestData(val foo: Int)

  @Test
  fun `GET - response is pretty-printed JSON`() = testApplication {
    application {
      configureSerialization()
      routing { get("/test") { call.respond(TestData(foo = 42)) } }
    }

    val response = client.get("/test")
    assertEquals(HttpStatusCode.OK, response.status)

    val bodyText = response.bodyAsText()
    assertTrue(bodyText.contains("\n"), "Response should contain newlines")
    assertTrue(bodyText.trim().startsWith("{"), "Response should start with '{'")
    assertTrue(bodyText.contains("\"foo\": 42"), "Response JSON should include foo = 42")
    assertTrue(bodyText.trim().endsWith("}"), "Response should end with '}'")
  }

  @Test
  fun `POST - unknown JSON keys are ignored`() = testApplication {
    application {
      configureSerialization()
      routing {
        post("/test") {
          val data = call.receive<TestData>()
          call.respond(data)
        }
      }
    }

    val response =
        client.post("/test") {
          contentType(ContentType.Application.Json)
          setBody("""{"foo":1,"bar":2}""")
        }

    assertEquals(HttpStatusCode.OK, response.status)
    val bodyText = response.bodyAsText()
    assertTrue(bodyText.contains("\"foo\": 1"), "Response should include foo = 1")
    assertFalse(bodyText.contains("bar"), "Response should not include unknown key 'bar'")
  }
}
