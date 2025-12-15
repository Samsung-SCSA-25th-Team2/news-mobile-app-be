package com.example.news.article.service

import com.example.news.article.domain.Article
import com.example.news.article.domain.ArticleSection
import com.example.news.article.dto.ArticleResponse
import com.example.news.article.dto.mapper.toResponse
import com.example.news.article.exception.ArticleNotFoundException
import com.example.news.article.repository.ArticleRepository
import com.example.news.article.repository.ReactionRepository
import com.example.news.common.dto.pagination.PageResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ArticleService(
    private val articleRepository: ArticleRepository,
    private val reactionRepository: ReactionRepository
) {

    /**
     * 사용자의 반응을 조회하는 헬퍼 메서드
     */
    private fun getUserReaction(articleId: Long, userId: Long?): String? {
        if (userId == null) return null
        val reaction = reactionRepository.findByArticleArticleIdAndUserId(articleId, userId)
        return reaction?.type?.name
    }

    fun getArticelById(
        articleId: Long,
        userId: Long? = null
    ): ArticleResponse {
        val article = articleRepository.findArticleByArticleId(articleId)

        if (article == null) {
            throw ArticleNotFoundException("해당 기사가 없습니다.")
        }

        val userReaction = getUserReaction(articleId, userId)
        return article.toResponse(userReaction)
    }

    fun getAllArticlesBySection(
        section: ArticleSection,
        page: Int,
        size: Int,
        userId: Long? = null
    ): PageResponse<ArticleResponse> {

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "publishedAt", "articleId")
        )
        val findAllBySection = articleRepository.findAllBySection(section, pageable)
        val content = findAllBySection.content.map { article ->
            val userReaction = getUserReaction(article.articleId!!, userId)
            article.toResponse(userReaction)
        }

        return PageResponse(
            content = content,
            page = findAllBySection.number,
            size = findAllBySection.size,
            totalElements = findAllBySection.totalElements,
            totalPages = findAllBySection.totalPages,
            last = findAllBySection.isLast
        )
    }

    fun getRandomArticleBySectionAndDate(
        section: ArticleSection,
        date: LocalDate,
        userId: Long? = null
    ): ArticleResponse {

        val startOfDay: LocalDateTime = date.atStartOfDay()
        val endOfDay = date.plusDays(1).atStartOfDay()  // 다음날 00:00:00

        val articles = articleRepository.findBySectionAndPublishedAtBetween(
            section,
            startOfDay,
            endOfDay
        )

        if (articles.isEmpty()) {
            throw ArticleNotFoundException("해당 날짜에 ${section} 섹션 기사가 없습니다.")
        }

        val randomArticle = articles.random()
        val userReaction = getUserReaction(randomArticle.articleId!!, userId)
        return randomArticle.toResponse(userReaction)
    }

}