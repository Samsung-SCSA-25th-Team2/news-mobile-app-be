package com.example.news.article.controller

import com.example.news.article.dto.ReactionRequest
import com.example.news.article.service.ReactionService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/articles/")
class ReactionController(
    private val reactionService: ReactionService
) {

    /**
     * 기사 좋아요/싫어요/해제
     * - POST /api/v1/articles/{articleId}/reaction
     *   Body: { "type": "LIKE" | "DISLIKE" | "NONE" }
     */
    @PostMapping("/{articleId}/reaction")
    fun react(
        @PathVariable articleId: Long,
        @AuthenticationPrincipal principal: UserDetails,
        @RequestBody request: ReactionRequest
    ): ResponseEntity<Unit> {

        val userId = principal.username.toLong()

        reactionService.react(articleId, userId, request.type)

        return ResponseEntity.ok().build()
    }

}











