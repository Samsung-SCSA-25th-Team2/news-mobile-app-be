package com.example.news.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(): OpenAPI {
        val jwtSchemeName = "JWT Authorization"
        val securityRequirement = SecurityRequirement().addList(jwtSchemeName)

        val components = Components()
            .addSecuritySchemes(
                jwtSchemeName,
                SecurityScheme()
                    .name(jwtSchemeName)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("JWT 토큰을 입력하세요 (Bearer 접두사 없이)")
            )

        return OpenAPI()
            .info(
                Info()
                    .title("MyNews Mobile App API")
                    .version("v1.0")
                    .description("""
                        뉴스 모바일 앱 백엔드 API 명세서

                        ## 주요 기능
                        - 사용자 인증 및 회원가입 (JWT 기반)
                        - 뉴스 기사 조회 (섹션별, 랜덤)
                        - 기사 반응 (좋아요/싫어요)
                        - 북마크 관리

                        ## 인증 방법
                        1. `/api/v1/auth/login` 또는 `/api/v1/auth/signup`으로 로그인/회원가입
                        2. 응답으로 받은 `accessToken`을 복사
                        3. 우측 상단 **Authorize** 버튼 클릭
                        4. `accessToken` 값을 입력 (Bearer 접두사 없이)
                        5. 인증이 필요한 API 호출 가능
                    """.trimIndent())
                    .contact(
                        Contact()
                            .name("Samsung SCSA 25th Team2")
                            .email("team2@example.com")
                    )
            )
            .addServersItem(
                Server()
                    .url("https://was-my-news-mobile-app-5867658782.us-central1.run.app")
                    .description("운영 서버")
            )
            .components(components)
            .addSecurityItem(securityRequirement)
    }
}