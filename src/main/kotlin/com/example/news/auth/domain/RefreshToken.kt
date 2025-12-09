package com.example.news.auth.domain

import org.springframework.data.redis.core.RedisHash
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.TimeToLive

@RedisHash(value = "refresh_token")
class RefreshToken (

    @Id
    val userId: Long?,
    val token: String,

    @TimeToLive
    var ttl: Long? = null
)