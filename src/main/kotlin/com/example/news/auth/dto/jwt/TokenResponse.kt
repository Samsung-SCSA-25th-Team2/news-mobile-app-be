package com.example.news.auth.dto.jwt

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "JWT 토큰 응답")
data class TokenResponse(
    @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String,

    @Schema(description = "토큰 타입", example = "Bearer", defaultValue = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "액세스 토큰 만료 시간 (밀리초)", example = "900000")
    val accessTokenExpiresIn: Long
)
