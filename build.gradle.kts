plugins {
    kotlin("jvm") version "1.8.10"
    kotlin("plugin.serialization") version "1.8.10"
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.ncorti.ktfmt.gradle") version "0.11.0"
    application
    jacoco
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(libs.guava)
    implementation("io.ktor:ktor-server-core-jvm:2.2.4")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.4")
    implementation("ch.qos.logback:logback-classic:1.2.11")
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")

    implementation("io.ktor:ktor-client-core-jvm:2.2.4")
    implementation("io.ktor:ktor-client-cio-jvm:2.2.4")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:2.2.4")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:2.2.4")

    implementation("com.github.poplopok:Logger:1.0.6")

    implementation("io.insert-koin:koin-core:3.3.3")
    implementation("io.insert-koin:koin-ktor:3.3.1")
    implementation("io.insert-koin:koin-logger-slf4j:3.3.0")
    implementation("io.ktor:ktor-server-call-logging:2.2.4")

    testImplementation("io.mockk:mockk:1.13.5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
    testImplementation("io.ktor:ktor-server-test-host:2.2.4")
    testImplementation("io.ktor:ktor-client-mock:2.2.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.jar {
  manifest {
    attributes["Main-Class"] = application.mainClass.get()
  }
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  from({
    configurations
      .runtimeClasspath
      .get()
      .filter { it.name.endsWith(".jar") }
      .map { zipTree(it) }
  })
}

application {
    mainClass.set("com.mad.feed.ApplicationKt")
}

jacoco {
    toolVersion = "0.8.10"
}

// Список директорий классов, исключая модели и DTO из покрытия
val coverageClassDirs = fileTree("${buildDir}/classes/java/main") {
    exclude(
        "com/mad/feed/dto/**",
        "com/mad/feed/models/**"
    )
}

tasks {
    // Настройка тестового раннера и логирования
    test {
        useJUnitPlatform()
        testLogging {
            events = setOf(
                org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED,
                org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
            )
            exceptionFormat = 
                org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }

    // Генерация отчёта покрытия
    jacocoTestReport {
        dependsOn(test)
        classDirectories.setFrom(coverageClassDirs)
        reports {
            xml.required.set(false)
            html.required.set(false)
            csv.required.set(false)
        }
    }

    // Проверка минимального порога покрытия
    jacocoTestCoverageVerification {
        dependsOn(test)
        classDirectories.setFrom(coverageClassDirs)
        violationRules {
            rule {
                limit {
                    minimum = "0.60".toBigDecimal()
                }
            }
        }
    }

    // Интеграция проверки покрытия в lifecycle
    check {
        dependsOn(jacocoTestCoverageVerification)
    }
    build {
        dependsOn(jacocoTestCoverageVerification)
    }
}
