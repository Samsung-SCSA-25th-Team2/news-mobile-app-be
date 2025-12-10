package com.example.news.article.service

import com.example.news.article.domain.ArticleSection
import com.example.news.article.dto.ArticleResponse
import com.example.news.article.dto.mapper.toResponse
import com.example.news.article.exception.ArticleNotFoundException
import com.example.news.article.repository.ArticleRepository
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
    private val articleRepository: ArticleRepository
) {

    fun getAllArticlesBySection(
        section: ArticleSection,
        page: Int,
        size: Int
    ): PageResponse<ArticleResponse> {

        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "publishedAt", "id")
        )
        val findAllBySection = articleRepository.findAllBySection(section, pageable)
        val content = findAllBySection.content.map { it.toResponse() }

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
        date: LocalDate
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

        return articles.random().toResponse()
    }

}