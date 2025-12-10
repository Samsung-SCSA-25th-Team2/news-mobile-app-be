package com.example.news.common.dto

import java.time.LocalDateTime

/**
 * 에러 응답 DTO
 */
data class ErrorResponse(
    val message: String?,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
