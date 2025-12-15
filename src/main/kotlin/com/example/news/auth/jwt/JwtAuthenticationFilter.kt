package com.example.news.auth.jwt

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 매 HTTP 요청마다 실행되면서,
 * 헤더에서 AccessToken(JWT)을 꺼내 검증하고,
 * 유효하면 SecurityContext에 "로그인 정보(Authentication)"를 넣어주는 필터
 */
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() { // 요청당 한 번만 실행되는 필터 -> 같은 요청에 필터 검증을 중복을 방지

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    // 모든 Http 요청이 여기로 들어옴
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestUri = request.requestURI

        try {
            // request 헤더의 토큰 꺼내기
            val token: String? = resolveToken(request)

            if (token == null) {
                logger.debug("JWT 토큰이 없습니다. URI: {}", requestUri)
            } else {
                // 토큰 검증
                if (jwtTokenProvider.validateAccessToken(token)) {
                    // 검증값 꺼내기
                    val authentication = jwtTokenProvider.getAuthentication(token)
                    // SecurityContextHolder에 넣기 -> 나중에 Service에서 쉽게 사용가능
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("JWT 인증 성공. 사용자: {}, URI: {}", authentication.name, requestUri)
                } else {
                    logger.warn("JWT 토큰 검증 실패. URI: {}", requestUri)
                }
            }
        } catch (e: Exception) {
            logger.error("JWT 필터 처리 중 에러 발생. URI: {}, 에러: {}", requestUri, e.message, e)
        }

        // 필터 체인 내 다음 필터로 넘기기
        filterChain.doFilter(request, response)
    }

    // request 헤더의 accessToken 꺼내는 헬퍼 함수
    private fun resolveToken(request: HttpServletRequest): String? {
        // request 헤더 "Authorization"에 bearer token 있는지 검증
        val bearer = request.getHeader("Authorization") ?: return null

        return if (bearer.startsWith("Bearer ", ignoreCase = true)) {
            bearer.substring(7)
        } else null
    }

}