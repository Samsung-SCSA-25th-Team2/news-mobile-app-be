package com.example.news.bookmark.repository

import com.example.news.bookmark.domain.Bookmark
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface BookmarkRepository : JpaRepository<Bookmark, Long> {

    /**
     * 특정 사용자의 모든 북마크 조회 (내 북마크 목록)
     */
    fun findByUserUserId(userId: Long): List<Bookmark>

    /**
     * 특정 사용자가 특정 기사를 북마크했는지 확인
     */
    fun existsByUserUserIdAndArticleArticleId(userId: Long, articleId: Long): Boolean

    /**
     * 특정 사용자의 북마크 목록을 생성 시간의 내림차순으로 정렬하여 페이지네이션하여 조회합니다.
     *
     * @param userId 북마크를 조회할 사용자의 ID
     * @param pageable 페이지네이션 및 정렬 정보
     */
    fun findByUserUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Bookmark>

    /**
     * 특정 사용자가 특정 기사를 북마크한 엔티티 조회 (삭제용)
     */
    fun findByUserUserIdAndArticleArticleId(userId: Long, articleId: Long): Bookmark?

    /**
     * 특정 사용자의 북마크 삭제
     */
    fun deleteBookmarkByUserUserIdAndArticleArticleId(userId: Long, articleId: Long)

    /**
     * 사용자 삭제 시 해당 사용자의 모든 북마크 삭제
     * (벌크 삭제로 성능 최적화)
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.user.userId = :userId")
    fun deleteAllByUserId(userId: Long)

    /**
     * 기사 삭제 시 해당 기사의 모든 북마크 삭제
     * (벌크 삭제로 성능 최적화)
     */
    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.article.articleId = :articleId")
    fun deleteAllByArticleId(articleId: Long)
}