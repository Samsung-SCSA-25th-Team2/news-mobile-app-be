package com.example.news.auth.jwt

import org.springframework.security.core.userdetails.User
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * JWT 인증의 모든 핵심 기능을 담당
 *
 * - AccessToken 생성
 * - RefreshToken 생성
 * - JWT 내부의 Claims(정보) 파싱
 * - 토큰 검증 (만료, 서명 체크 등)
 * - Spring Security 인증 객체(Authentication) 생성
 */
@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    // JWT 서명에 사용할 SecretKey 생성
    private val accessKey = Keys.hmacShaKeyFor(jwtProperties.accessTokenSecret.toByteArray())
    private val refreshKey = Keys.hmacShaKeyFor(jwtProperties.refreshTokenSecret.toByteArray())

    // accessToken 생성
    fun createAccessToken(userId: Long?, role: String = "ROLE_USER") : String {
        val now = Date()
        val expiration = Date(now.time + jwtProperties.accessTokenExpirationMs)

        return Jwts.builder()
            .setSubject(userId.toString()) // jwt 고유 식별자 -> userId
            .claim("role", role) // claim(jwt 속성)에 role 추가
            .setIssuedAt(now) // 발급시간
            .setExpiration(expiration) // 만료시간
            .signWith(accessKey, SignatureAlgorithm.HS256) // HS256 방식으로 서명
            .compact() // 하나의 String으로 합치기
    }

    // refreshToken 생성 -> accessToken 생성과 유사
    fun createRefreshToken(userId: Long?): String {
        val now = Date()
        val expiry = Date(now.time + jwtProperties.refreshTokenExpirationMs)

        return Jwts.builder()
            .setSubject(userId.toString())
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(refreshKey, SignatureAlgorithm.HS256)
            .compact()
    }

    // jwt -> spring security 인증 객체
    // accessToken에만 해당 !!!
    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token, accessKey)

        val userId = claims.subject.toLong()
        val role = claims["role"]?.toString() ?: "ROLE_USER"

        // authorities: 권한 "무엇을 할 수 있는가?"
        val authorities = listOf(SimpleGrantedAuthority(role))

        // principal: 인증된 사용자 정보 "누구인가?"
        // password는 token이 있어서 ""
        val principal = User(userId.toString(), "", authorities)

        // credentials: token "본인임을 증명하는 것"
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    // accessToken 검증
    fun validateAccessToken(token: String): Boolean =
        try {
            parseClaims(token, accessKey); true
        } catch (e: Exception) {
            false
        }

    // refreshToken 검증
    fun validateRefreshToken(token: String): Boolean =
        try {
            parseClaims(token, refreshKey); true
        } catch (e: Exception) {
            false
        }

    // accessToken에서 userId 꺼내기
    fun getUserIdFromAccessToken(token: String): Long =
        parseClaims(token, accessKey).subject.toLong()

    // refreshToken에서 userId 꺼내기
    fun getUserIdFromRefreshToken(token: String): Long =
        parseClaims(token, refreshKey).subject.toLong()

    // Jwt 내 claims를 파싱하는 헬퍼 함수
    private fun parseClaims(token: String, key: SecretKey): Claims =
        Jwts.parser()                // 최신 버전 엔트리 포인트
            .verifyWith(key)         // key = SecretKey (Keys.hmacShaKeyFor(...))
            .build()
            .parseSignedClaims(token)
            .payload                  // 옛날 .body 대신 .payload

}
