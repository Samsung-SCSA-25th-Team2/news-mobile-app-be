package com.example.news.article.dto

import java.time.LocalDateTime

data class ArticleResponse(
    val articleId: Long,
    val section: String,
    val title: String,
    val url: String,
    val thumbnailUrl: String?,
    val source: String?,
    val publisher: String?,
    val publishedAt: LocalDateTime
)
