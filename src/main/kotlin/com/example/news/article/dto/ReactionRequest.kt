package com.example.news.article.dto

import com.example.news.article.domain.ReactionType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "기사 반응 요청")
data class ReactionRequest(
    @field:NotNull(message = "리액션 타입은 필수입니다 (LIKE, DISLIKE, NONE)")
    @Schema(
        description = "반응 타입 (LIKE: 좋아요, DISLIKE: 싫어요, NONE: 반응 해제)",
        example = "LIKE",
        allowableValues = ["LIKE", "DISLIKE", "NONE"],
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    val type: ReactionType?
)
