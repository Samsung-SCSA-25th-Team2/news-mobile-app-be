package com.example.news.user.dto

data class UserResponse(
    val id: Long,
    val email: String,
    val bookmarkCount: Int
)
