package com.mad.feed.routes

import com.mad.feed.models.CreateCommentRequest
import com.mad.feed.models.ListCommentsResponse
import com.mad.feed.models.PaginationRequest
import com.mad.feed.services.CommentService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.configureCommentRoutes() {
    val commentService: CommentService by inject()
    
    route("/api/posts/{postId}/comments") {
        // Add a new comment to a post
        post {
            val postId = call.parameters["postId"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing post ID")
            val request = call.receive<CreateCommentRequest>()
            
            val comment = commentService.createComment(postId, request) 
                ?: return@post call.respond(HttpStatusCode.NotFound, "Post not found")
                
            call.respond(HttpStatusCode.Created, comment)
        }
        
        // List comments for a post
        get {
            val postId = call.parameters["postId"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing post ID")
            val pagination = call.receive<PaginationRequest>()
            
            val (comments, totalCount) = commentService.listComments(postId, pagination.page, pagination.pageSize)
            
            call.respond(
                ListCommentsResponse(
                    comments = comments,
                    totalCount = totalCount,
                    page = pagination.page,
                    pageSize = pagination.pageSize
                )
            )
        }
    }
}
