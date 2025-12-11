package com.example.news

import com.example.news.auth.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)
@EnableScheduling  // 스케줄러 활성화
class NewsApplication

fun main(args: Array<String>) {
	runApplication<NewsApplication>(*args)
}
