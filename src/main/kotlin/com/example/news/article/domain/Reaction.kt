package com.example.news.article.domain

import com.example.news.common.domain.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "article_reactions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_article_user",
            columnNames = ["article_id", "user_id"]
        )
    ]
)
class Reaction(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val articleReactionId: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    val article: Article,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ReactionType

) : BaseEntity()
