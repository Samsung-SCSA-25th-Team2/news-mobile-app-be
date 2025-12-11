package com.example.news.auth.dto.jwt

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "토큰 재발급 요청")
data class ReissueRequest(
    @field:NotBlank
    @Schema(description = "만료된 액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    val accessToken: String,

    @field:NotBlank
    @Schema(description = "유효한 리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", requiredMode = Schema.RequiredMode.REQUIRED)
    val refreshToken: String
)
