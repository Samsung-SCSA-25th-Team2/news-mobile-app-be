package com.example.news.config

import com.example.news.auth.jwt.JwtAuthenticationFilter
import com.example.news.auth.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider
) {

    @Bean
    fun filterChain(http: HttpSecurity) : SecurityFilterChain {
        http
            .csrf { it.disable() } // csrf : 웹 브라우저 폼 기반 공격
            .httpBasic { it.disable() } //
            .formLogin { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                it.requestMatchers(
                    "/api/v1/auth/signup",
                    "/api/v1/auth/login",
                    "/api/v1/auth/reissue",
                ).permitAll()
                    .anyRequest().authenticated()
            }

        // JWT 인증 필터를 시큐리티 필터 체인의 내부에 끼워넣는 것
        // 로그인 기반 인증보다 우리가 만든 JWT 인증이 먼저 실행되도록 하기 위해서
        http.addFilterBefore(
            JwtAuthenticationFilter(jwtTokenProvider),
            UsernamePasswordAuthenticationFilter::class.java
        )

        return http.build()
    }
}
