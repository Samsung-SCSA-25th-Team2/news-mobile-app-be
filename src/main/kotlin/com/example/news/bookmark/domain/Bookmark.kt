package com.example.news.bookmark.domain

import com.example.news.article.domain.Article
import com.example.news.common.domain.BaseEntity
import com.example.news.user.domain.User
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_bookmark_user_article",
            columnNames = ["user_id", "article_id"]
        )
    ]
)
class Bookmark (

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    val article: Article

) : BaseEntity()
