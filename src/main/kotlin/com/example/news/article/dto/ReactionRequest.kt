package com.example.news.article.dto

import com.example.news.article.domain.ReactionType

data class ReactionRequest(
    val type: ReactionType // LIKE, DISLIKE, NONE
)
