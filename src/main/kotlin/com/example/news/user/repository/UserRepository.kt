package com.example.news.user.repository

import com.example.news.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): User?
    fun existsByEmail(email: String): Boolean

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.user.userId = :userId")
    fun countBookmarksByUserId(userId: Long): Int
}
