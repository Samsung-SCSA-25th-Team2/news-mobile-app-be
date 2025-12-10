package com.example.news.user.dto.mapper

import com.example.news.user.domain.User
import com.example.news.user.dto.UserResponse

fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.userId,
        email = this.email,
        bookmarkCount = this.bookmarks.size
    )
