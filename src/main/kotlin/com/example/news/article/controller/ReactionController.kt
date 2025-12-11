package com.example.news.article.controller

import com.example.news.article.dto.ReactionRequest
import com.example.news.article.service.ReactionService
import com.example.news.common.dto.error.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "기사 반응 API", description = "기사 좋아요/싫어요 관련 API (JWT 필요)")
@RestController
@RequestMapping("/api/v1/articles/")
class ReactionController(
    private val reactionService: ReactionService
) {

    @Operation(
        summary = "기사 반응 추가/변경/삭제",
        description = """
            특정 기사에 대한 반응을 추가하거나 변경, 삭제합니다.
            **🔒 인증 필요** - JWT 토큰이 필요합니다.

            **반응 타입:**
            - LIKE: 좋아요
            - DISLIKE: 싫어요
            - NONE: 반응 해제

            **동작 방식:**
            - 기존 반응이 없으면 새로 추가
            - 기존 반응이 있으면 변경 (LIKE ↔ DISLIKE)
            - NONE을 보내면 반응 삭제
            - 동일한 반응을 다시 보내면 무시 (멱등성 보장)
        """,
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "반응 처리 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효하지 않은 반응 타입)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "기사를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/{articleId}/reaction")
    fun react(
        @Parameter(description = "기사 ID", example = "1", required = true)
        @PathVariable articleId: Long,
        @AuthenticationPrincipal principal: UserDetails,
        @Valid @RequestBody request: ReactionRequest
    ): ResponseEntity<Unit> {
        val userId = principal.username.toLong()
        reactionService.react(articleId, userId, request.type!!)
        return ResponseEntity.ok().build()
    }

}











