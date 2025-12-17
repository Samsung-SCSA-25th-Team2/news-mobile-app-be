package com.example.news.article.repository

import com.example.news.article.domain.Article
import com.example.news.article.domain.ArticleSection
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ArticleRepository : JpaRepository<Article, Long> {

    fun findArticleByArticleId(articleId: Long): Article?

    fun findAllBySection(
       section: ArticleSection,
       pageable: Pageable
    ): Page<Article> // 정렬은 Service에서 수행

    fun findBySectionAndPublishedAtBetween(
        section: ArticleSection,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Article>

    /**
     * URL 중복 검사 (크롤링 시 사용)
     */
    fun existsByUrl(url: String): Boolean

    @Query("select a.url from Article a where a.url in :urls")
    fun findExistingUrls(@Param("urls") urls: Set<String>): Set<String>

}