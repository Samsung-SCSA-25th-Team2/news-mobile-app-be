package com.example.news.article.dto

import com.example.news.article.domain.ReactionType
import jakarta.validation.constraints.NotNull

data class ReactionRequest(
    @field:NotNull(message = "리액션 타입은 필수입니다 (LIKE, DISLIKE, NONE)")
    val type: ReactionType? // LIKE, DISLIKE, NONE
)
