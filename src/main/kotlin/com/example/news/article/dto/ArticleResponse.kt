package com.example.news.article.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "기사 정보 응답")
data class ArticleResponse(
    @Schema(description = "기사 ID", example = "1")
    val articleId: Long,

    @Schema(description = "기사 섹션", example = "TECHNOLOGY")
    val section: String,

    @Schema(description = "기사 제목", example = "AI 기술의 최신 동향")
    val title: String,

    @Schema(description = "기사 URL", example = "https://example.com/article/123")
    val url: String,

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg", nullable = true)
    val thumbnailUrl: String?,

    @Schema(description = "기사 출처", example = "뉴스 제공사", nullable = true)
    val source: String?,

    @Schema(description = "기사 발행사", example = "출판사 이름", nullable = true)
    val publisher: String?,

    @Schema(description = "기사 발행 시간", example = "2025-12-11T10:30:00")
    val publishedAt: LocalDateTime
)
