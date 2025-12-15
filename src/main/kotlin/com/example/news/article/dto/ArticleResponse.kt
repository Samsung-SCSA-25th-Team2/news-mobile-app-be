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

    @Schema(description = "기사 본문", example = "RAG 기술이 현재 금융권에서 화두가 되고 있습니다. 하...")
    val content: String?,

    @Schema(description = "기사 URL", example = "https://example.com/article/123")
    val url: String,

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg", nullable = true)
    val thumbnailUrl: String?,

    @Schema(description = "기사 출처", example = "뉴스 제공사", nullable = true)
    val source: String?,

    @Schema(description = "기사 발행사", example = "출판사 이름", nullable = true)
    val publisher: String?,

    @Schema(description = "기사 발행 시간", example = "2025-12-11T10:30:00")
    val publishedAt: LocalDateTime,

    @Schema(description = "좋아요 수", example = "100")
    val likes: Long,

    @Schema(description = "싫어요 수", example = "50")
    val dislikes: Long,

    @Schema(description = "사용자의 반응 (LIKE, DISLIKE, null)", example = "LIKE", nullable = true)
    val userReaction: String? = null

)
