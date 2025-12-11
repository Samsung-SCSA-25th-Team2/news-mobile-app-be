package com.example.news.bookmark.service

import com.example.news.article.dto.ArticleResponse
import com.example.news.article.dto.mapper.toResponse
import com.example.news.article.exception.ArticleNotFoundException
import com.example.news.article.repository.ArticleRepository
import com.example.news.bookmark.domain.Bookmark
import com.example.news.bookmark.repository.BookmarkRepository
import com.example.news.common.dto.pagination.PageResponse
import com.example.news.user.exception.UserNotFoundException
import com.example.news.user.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class BookmarkService(
    private val bookmarkRepository: BookmarkRepository,
    private val userRepository: UserRepository,
    private val articleRepository: ArticleRepository
) {

    /**
     * 북마크 추가
     */
    @Transactional
    fun addBookmark(userId: Long, articleId: Long) {
        // 이미 북마크 등록되어 있으면 아무것도 안함
        if (bookmarkRepository.existsByUserUserIdAndArticleArticleId(userId, articleId)) {
            return
        }

        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("해당 유저가 존재하지 않습니다.") }

        val article = articleRepository.findById(articleId)
            .orElseThrow() { ArticleNotFoundException("해당 기사가 존재하지 않습니다.") }

        val bookmark = Bookmark(
            user = user,
            article = article
        )

        bookmarkRepository.save(bookmark)
    }

    /**
     * 북마크 삭제
     */
    @Transactional
    fun removeBookmark(userId: Long, articleId: Long) {
        bookmarkRepository.deleteBookmarkByUserUserIdAndArticleArticleId(userId, articleId)
        // 존재하지 않아도 ok -> 멱등
    }

    /**
     * 내가 북마크한 기사 목록 조회 (최신 북마크 순)
     */
    fun getMyBookmarks(
        userId: Long,
        page: Int,
        size: Int
    ): PageResponse<ArticleResponse> {
        
        // pageable 생성 (정렬은 메서드명에 포함되어 있으므로 별도 지정 불필요)
        val pageable = PageRequest.of(page, size)

        // Page 조회
        val bookmarkPage = bookmarkRepository.findByUserUserIdOrderByCreatedAtDesc(
            userId = userId,
            pageable = pageable
        )

        // Page 안의 Bookmark -> Article DTO 변환
        val articleResponses = bookmarkPage.content
            .map { it.article.toResponse() }

        return PageResponse(
            content = articleResponses,
            page = bookmarkPage.number,
            size = bookmarkPage.size,
            totalElements = bookmarkPage.totalElements,
            totalPages = bookmarkPage.totalPages,
            last = bookmarkPage.isLast
        )

    }
}