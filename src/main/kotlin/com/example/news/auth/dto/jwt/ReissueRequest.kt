package com.example.news.auth.dto.jwt

data class ReissueRequest(
    val accessToken: String,
    val refreshToken: String
)
