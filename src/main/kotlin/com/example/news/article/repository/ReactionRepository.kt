package com.example.news.article.repository

import com.example.news.article.domain.Reaction
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionRepository : JpaRepository<Reaction, Long> {
    fun findByArticleArticleIdAndUserId(articleId: Long, userId: Long): Reaction?
}