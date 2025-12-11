package com.example.news.article.domain

import com.example.news.common.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
class Article (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val articleId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val section: ArticleSection,

    @Column(nullable = false)
    val title: String,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    @Column(nullable = false, unique = true)
    val url: String,

    @Column(nullable = true)
    val thumbnailUrl: String? = null,

    @Column(nullable = true)
    val source: String? = null,

    @Column(nullable = true)
    val publisher: String? = null,

    @Column(nullable = false)
    val publishedAt: LocalDateTime,

    // ✅ 제거: 양방향 관계 불필요
    // "내 북마크만 조회"하므로 BookmarkRepository.findByUserId()로 충분
    // Article 삭제 시 Bookmark는 @OnDelete(CASCADE) 또는 BookmarkRepository로 처리

    // ✅ 좋아요 / 싫어요 카운트
    @Column(nullable = false)
    var likes: Long = 0L,

    @Column(nullable = false)
    var dislikes: Long = 0L,

    // ✅ Optimistic Lock (버전 필드)
    @Version
    var version: Long? = null

) : BaseEntity()