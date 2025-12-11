package com.example.news.bookmark.controller

import com.example.news.article.dto.ArticleResponse
import com.example.news.bookmark.service.BookmarkService
import com.example.news.common.dto.error.ErrorResponse
import com.example.news.common.dto.pagination.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "북마크 API", description = "기사 북마크 관련 API (JWT 필요)")
@Validated
@RestController
@RequestMapping("/api/v1/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
){

    @Operation(
        summary = "북마크 추가",
        description = """
            특정 기사를 북마크에 추가합니다.
            **🔒 인증 필요** - JWT 토큰이 필요합니다.

            - 이미 북마크된 기사는 무시 (멱등성 보장)
            - 북마크 추가 시 자동으로 생성 시간이 기록됨
        """,
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "북마크 추가 성공"
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
    @PostMapping("/{articleId}")
    fun addBookmark(
        @Parameter(description = "북마크할 기사 ID", example = "1", required = true)
        @PathVariable articleId: Long,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {
        val userId = principal.username.toLong()
        bookmarkService.addBookmark(userId, articleId)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "북마크 삭제",
        description = """
            특정 기사를 북마크에서 삭제합니다.
            **🔒 인증 필요** - JWT 토큰이 필요합니다.

            - 북마크가 존재하지 않아도 성공 응답 (멱등성 보장)
        """,
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "북마크 삭제 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @DeleteMapping("/{articleId}")
    fun removeBookmark(
        @Parameter(description = "삭제할 북마크의 기사 ID", example = "1", required = true)
        @PathVariable articleId: Long,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {
        val userId = principal.username.toLong()
        bookmarkService.removeBookmark(userId, articleId)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "내 북마크 목록 조회",
        description = """
            현재 로그인한 사용자의 북마크 목록을 조회합니다. 최신 북마크 순으로 정렬됩니다.
            **🔒 인증 필요** - JWT 토큰이 필요합니다.
        """,
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "북마크 목록 조회 성공",
                content = [Content(schema = Schema(implementation = PageResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효하지 않은 페이지 파라미터)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/me")
    fun getMyBookmarks(
        @AuthenticationPrincipal principal: UserDetails,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "페이지 크기 (최대 100)", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int
    ): ResponseEntity<PageResponse<ArticleResponse>> {
        val userId = principal.username.toLong()
        val result = bookmarkService.getMyBookmarks(userId, page, size)
        return ResponseEntity.ok(result)
    }

}