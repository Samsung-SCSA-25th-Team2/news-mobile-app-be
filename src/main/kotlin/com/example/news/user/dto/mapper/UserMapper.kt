package com.example.news.user.dto.mapper

import com.example.news.user.domain.User
import com.example.news.user.dto.UserResponse

/**
 * User -> UserResponse 변환 (최적화 버전)
 * bookmarkCount를 별도로 전달받아 N+1 문제를 방지합니다.
 */
fun User.toResponse(bookmarkCount: Int): UserResponse =
    UserResponse(
        id = this.userId!!,
        email = this.email,
        bookmarkCount = bookmarkCount
    )
