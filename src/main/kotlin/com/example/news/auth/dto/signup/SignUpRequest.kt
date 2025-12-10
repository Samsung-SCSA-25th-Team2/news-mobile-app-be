package com.example.news.auth.dto.signup

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String
)
