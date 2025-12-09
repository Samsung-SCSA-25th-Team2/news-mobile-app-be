package com.example.news

import com.example.news.auth.jwt.JwtProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties::class)

class NewsApplication

fun main(args: Array<String>) {
	runApplication<NewsApplication>(*args)
}
