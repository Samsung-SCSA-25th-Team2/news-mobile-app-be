package com.example.news.auth.jwt

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "jwt")
// application.yaml에서 jwt 값을 자동으로 매핑해서 객체로 만들어준다 - JwtProperties 객체에 자동 바인딩
data class JwtProperties(
    // data class는 파라미터에 기본값이 필요함

    val accessTokenSecret: String = "",
    val refreshTokenSecret: String = "",
    val accessTokenExpirationMs: Long = 0,
    val refreshTokenExpirationMs: Long = 0
)