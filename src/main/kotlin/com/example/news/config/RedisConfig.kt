package com.example.news.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@Configuration
@EnableRedisRepositories(basePackages = ["com.example.news.auth.repository"])
// 해당 패키지에서 RedisRepository를 찾아, Redis 전용 Repository Bean 자동 생성
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        // `RedisTemplate`는 Redis의 JPA

        template.setConnectionFactory(connectionFactory)
        // RedisConnectionFactory 에서 .yaml의 redis 관련 속성 불러와서 주입

        return template
    }

}