package com.example.news.article.dto.mapper

import com.example.news.article.domain.Article
import com.example.news.article.dto.ArticleResponse

fun Article.toResponse() = ArticleResponse(
    articleId = this.articleId,
    section = this.section.name,
    title = this.title,
    url = this.url,
    thumbnailUrl = this.thumbnailUrl,
    source = this.source,
    publisher = this.publisher,
    publishedAt = this.publishedAt
)