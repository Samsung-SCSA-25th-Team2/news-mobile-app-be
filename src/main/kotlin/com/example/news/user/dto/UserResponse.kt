package com.example.news.user.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "사용자 정보 응답")
data class UserResponse(
    @Schema(description = "사용자 ID", example = "1")
    val id: Long,

    @Schema(description = "이메일 주소", example = "user@example.com")
    val email: String,

    @Schema(description = "북마크한 기사 수", example = "5")
    val bookmarkCount: Int
)
