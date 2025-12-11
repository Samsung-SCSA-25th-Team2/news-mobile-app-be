package com.example.news.bookmark.controller

import com.example.news.article.dto.ArticleResponse
import com.example.news.bookmark.service.BookmarkService
import com.example.news.common.dto.pagination.PageResponse
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

@Validated
@RestController
@RequestMapping("/api/v1/bookmarks")
class BookmarkController(
    private val bookmarkService: BookmarkService
){

    /**
     * 기사 북마크 추가
     */
    @PostMapping("/{articleId}")
    fun addBookmark(
         @PathVariable articleId: Long,
         @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {

        val userId = principal.username.toLong()
        bookmarkService.addBookmark(userId, articleId)

        return ResponseEntity.ok().build()
    }

    /**
     * 기사 북마크 삭제
     */
    @DeleteMapping("/{articleId}")
    fun removeBookmark(
        @PathVariable articleId: Long,
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {

        val userId = principal.username.toLong()
        bookmarkService.removeBookmark(userId, articleId)

        return ResponseEntity.ok().build()
    }

    /**
     * 내가 북마크한 기사 목록 조회 (최신 북마크 순)
     * 예: GET /api/v1/bookmarks/me?page=0&size=20
     */
    @GetMapping("/me")
    fun getMyBookmarks(
        @AuthenticationPrincipal principal: UserDetails,
        @RequestParam(defaultValue = "0") @Min(0) page: Int,        // ?page=0
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int        // ?size=20 (최대 100)
    ): ResponseEntity<PageResponse<ArticleResponse>> {

        val userId = principal.username.toLong()
        val result = bookmarkService.getMyBookmarks(userId, page, size)

        return ResponseEntity.ok(result)
    }

}