package com.example.news.article.domain

import com.example.news.bookmark.domain.Bookmark
import com.example.news.common.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "articles")
class Article (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val articleId: Long = 0L,

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

    @OneToMany(mappedBy = "article", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookmarks: MutableList<Bookmark> = mutableListOf()

) : BaseEntity()