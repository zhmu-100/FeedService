package com.mad.feed.routes

import com.mad.feed.models.AddReactionRequest
import com.mad.feed.models.RemoveReactionRequest
import com.mad.feed.services.ReactionService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureReactionRoutes() {
    val reactionService: ReactionService by inject()
    
    route("/api/posts/{postId}/reactions") {
        // Add a reaction to a post
        post {
            val postId = call.parameters["postId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing post ID")
            val request = call.receive<AddReactionRequest>()
            
            val reaction = reactionService.addReaction(postId, request)
            call.respond(HttpStatusCode.Created, reaction)
        }
        
        // Remove a reaction from a post
        delete {
            val postId = call.parameters["postId"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing post ID")
            val request = call.receive<RemoveReactionRequest>()
            
            val success = reactionService.removeReaction(postId, request.userId)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, "Reaction not found")
            }
        }
    }
}
