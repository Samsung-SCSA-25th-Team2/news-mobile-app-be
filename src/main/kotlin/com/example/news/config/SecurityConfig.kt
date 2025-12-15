package com.example.news.config

import com.example.news.auth.jwt.JwtAuthenticationFilter
import com.example.news.auth.jwt.JwtTokenProvider
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
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
                    "/api/v1/articles",
                    "/api/v1/articles/**",
                    "/api/v1/articles/*/random",
                    "/swagger-ui/**",                // Swagger UI
                    "/v3/api-docs/**",               // OpenAPI 스펙
                    "/swagger-resources/**"          // Swagger 리소스
                ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exception ->
                // 인증 실패 (토큰 없음/만료/유효하지 않음) -> 401 Unauthorized
                exception.authenticationEntryPoint { _, response, authException ->
                    response.status = HttpServletResponse.SC_UNAUTHORIZED
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    response.writer.write(
                        """{"message":"인증이 필요합니다. 로그인 후 다시 시도해주세요.","timestamp":"${java.time.LocalDateTime.now()}"}"""
                    )
                }

                // 인가 실패 (권한 부족) -> 403 Forbidden
                exception.accessDeniedHandler { _, response, accessDeniedException ->
                    response.status = HttpServletResponse.SC_FORBIDDEN
                    response.contentType = MediaType.APPLICATION_JSON_VALUE
                    response.characterEncoding = "UTF-8"
                    response.writer.write(
                        """{"message":"접근 권한이 없습니다.","timestamp":"${java.time.LocalDateTime.now()}"}"""
                    )
                }
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
