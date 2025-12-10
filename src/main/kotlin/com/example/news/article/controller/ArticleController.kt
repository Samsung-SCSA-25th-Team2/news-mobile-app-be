package com.example.news.article.controller

import com.example.news.article.domain.ArticleSection
import com.example.news.article.dto.ArticleResponse
import com.example.news.article.service.ArticleService
import com.example.news.common.dto.pagination.PageResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/articles")
class ArticleController(
    private val articleService : ArticleService
) {

    /**
     * 섹션 기준 기사 리스트 조회 (최신순 + 페이징)
     * 예: GET /api/v1/articles?section=TECHNOLOGY&page=0&size=20
     */
    @GetMapping
    fun getArticlesBySection(
        @RequestParam section: ArticleSection,              // ?section=TECHNOLOGY
        @RequestParam(defaultValue = "0") page: Int,        // ?page=0
        @RequestParam(defaultValue = "20") size: Int        // ?size=20
    ): ResponseEntity<PageResponse<ArticleResponse>> {

        val content = articleService.getAllArticlesBySection(section, page, size)

        return ResponseEntity.ok(content)
    }

    /**
     * 섹션 + 날짜 기준으로, 해당 날짜의 기사 중 랜덤 1개 반환
     *
     * 예:
     *  GET /api/v1/articles/TECHNOLOGY/random
     *  GET /api/v1/articles/TECHNOLOGY/random?date=2025-12-10
     */
    @GetMapping("/{section}/random")
    fun getRandomArticleBySection(
        @PathVariable section: ArticleSection,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<ArticleResponse> {

        val targetDate = date ?: LocalDate.now()

        val randomArticle = articleService.getRandomArticleBySectionAndDate(
            section = section,
            date = targetDate
        )

        return ResponseEntity.ok(randomArticle)
    }

}