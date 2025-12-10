package com.example.news.user.domain

import com.example.news.bookmark.domain.Bookmark
import com.example.news.common.domain.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "users")
class User(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0L,

    @Column(nullable = false, unique = true)
    var email: String = "",

    @Column(nullable = false)
    var password: String = "",

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val bookmarks: MutableList<Bookmark> = mutableListOf()

) : BaseEntity()
