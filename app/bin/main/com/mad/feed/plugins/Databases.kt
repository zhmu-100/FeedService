package com.mad.feed.plugins

import com.mad.feed.models.tables.*
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/postgres"
    val databaseUser = System.getenv("DATABASE_USER") ?: "postgres"
    val databasePassword = System.getenv("DATABASE_PASSWORD") ?: "postgres"
    
    val database = Database.connect(
        url = databaseUrl,
        driver = "org.postgresql.Driver",
        user = databaseUser,
        password = databasePassword
    )
    
    transaction(database) {
        SchemaUtils.create(
            Posts,
            PostAttachments,
            PostComments,
            PostReactions,
            CommentReactions
        )
    }
}
