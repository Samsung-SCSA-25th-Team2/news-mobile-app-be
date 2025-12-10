package com.example.news.auth.dto.jwt

import jakarta.validation.constraints.NotBlank

data class ReissueRequest(
    @field:NotBlank
    val accessToken: String,

    @field:NotBlank
    val refreshToken: String
)
