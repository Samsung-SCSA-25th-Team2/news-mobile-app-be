package com.example.news.article.controller

import com.example.news.article.domain.ArticleSection
import com.example.news.article.dto.ArticleResponse
import com.example.news.article.service.ArticleService
import com.example.news.common.dto.error.ErrorResponse
import com.example.news.common.dto.pagination.PageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "기사 API", description = "뉴스 기사 조회 관련 API (인증 불필요)")
@Validated
@RestController
@RequestMapping("/api/v1/articles")
class ArticleController(
    private val articleService : ArticleService
) {

    @Operation(
        summary = "섹션별 기사 목록 조회",
        description = """
            특정 섹션의 기사 목록을 최신순으로 조회합니다. 페이지네이션을 지원합니다.
            **🔓 인증 불필요** - 누구나 기사를 조회할 수 있습니다.

            **섹션 종류:**
            - POLITICS (정치)
            - ECONOMY (경제)
            - SOCIAL (사회)
            - TECHNOLOGY (IT/기술)
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "기사 목록 조회 성공",
                content = [Content(schema = Schema(implementation = PageResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효하지 않은 섹션 또는 페이지 파라미터)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping
    fun getArticlesBySection(
        @Parameter(
            description = "기사 섹션 (POLITICS, ECONOMY, SOCIAL, TECHNOLOGY)",
            example = "TECHNOLOGY",
            required = true
        )
        @RequestParam section: ArticleSection,
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @Parameter(description = "페이지 크기 (최대 100)", example = "20")
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) size: Int
    ): ResponseEntity<PageResponse<ArticleResponse>> {
        val content = articleService.getAllArticlesBySection(section, page, size)
        return ResponseEntity.ok(content)
    }

    @Operation(
        summary = "섹션별 랜덤 기사 조회",
        description = """
            특정 섹션의 특정 날짜 기사 중 랜덤으로 1개를 반환합니다.
            날짜를 지정하지 않으면 오늘 날짜의 기사를 조회합니다.
            **🔓 인증 불필요** - 누구나 기사를 조회할 수 있습니다.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "랜덤 기사 조회 성공",
                content = [Content(schema = Schema(implementation = ArticleResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효하지 않은 섹션 또는 날짜)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "해당 조건의 기사를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/{section}/random")
    fun getRandomArticleBySection(
        @Parameter(
            description = "기사 섹션 (POLITICS, ECONOMY, SOCIAL, TECHNOLOGY)",
            example = "TECHNOLOGY",
            required = true
        )
        @PathVariable section: ArticleSection,
        @Parameter(
            description = "조회할 날짜 (ISO 8601 형식: yyyy-MM-dd). 지정하지 않으면 오늘 날짜",
            example = "2025-12-10"
        )
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