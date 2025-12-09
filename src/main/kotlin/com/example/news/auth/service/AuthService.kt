package com.example.news.auth.service

import com.example.news.auth.domain.RefreshToken
import com.example.news.auth.dto.jwt.ReissueRequest
import com.example.news.auth.dto.jwt.TokenResponse
import com.example.news.auth.dto.login.LoginRequest
import com.example.news.auth.dto.signup.SignUpRequest
import com.example.news.auth.jwt.JwtProperties
import com.example.news.auth.jwt.JwtTokenProvider
import com.example.news.auth.repository.RefreshTokenRepository
import com.example.news.user.domain.User
import com.example.news.user.repository.UserRepository
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository, // ← Redis
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties
) {

    // 회원가입
    @Transactional
    fun signup(request: SignUpRequest) {
        // Validation
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다.")
        }

        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password)!!,
            nickname = request.nickname
        )

        userRepository.save(user)
    }

    // 로그인
    @Transactional
    fun login(request: LoginRequest): TokenResponse {
        // Validation
        val user = userRepository.findByEmail(request.email)
            ?: throw IllegalArgumentException("가입되지 않은 이메일입니다.")

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.")
        }

        // JWT 토큰 생성
        val accessToken = jwtTokenProvider.createAccessToken(user.id)
        val refreshToken = jwtTokenProvider.createRefreshToken(user.id)

        // RefreshToken Redis에 저장
        refreshTokenRepository.deleteByUserId(user.id)
        refreshTokenRepository.save(
            RefreshToken(
                userId = user.id,
                token = refreshToken,
                ttl = jwtProperties.refreshTokenExpirationMs / 1000
            )
        )

        // accessToken + refreshToken 응답
        return TokenResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpirationMs
        )
    }

    @Transactional
    fun reissue(request: ReissueRequest): TokenResponse {
        // Validation
        if (!jwtTokenProvider.validateRefreshToken(request.refreshToken)) {
            throw IllegalArgumentException("리프레시 토큰이 유효하지 않습니다.")
        }

        // refreshToken에서 userId 꺼내기
        val userId = jwtTokenProvider.getUserIdFromRefreshToken(request.refreshToken)

        // Redis에서 조회
        val saved = refreshTokenRepository.findByUserId(userId)
            ?: throw IllegalArgumentException("저장된 리프레시 토큰이 없습니다. 다시 로그인 해주세요.")

        // refreshToken 검증
        if (saved.token != request.refreshToken) {
            throw IllegalArgumentException("리프레시 토큰이 저장소의 것과 일치하지 않습니다.")
        }

        // accessToken + refreshToken 재발급
        // refreshToken 회전시켜서, 좀 더 안정성 증가
        val newAccessToken = jwtTokenProvider.createAccessToken(userId)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(userId)

        // Redis에 기존 RT 덮어쓰기 (Rotation)
        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                token = newRefreshToken,
                ttl = jwtProperties.refreshTokenExpirationMs / 1000
            )
        )

        return TokenResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtProperties.accessTokenExpirationMs
        )
    }

    @Transactional
    fun logout(authentication: Authentication) {
        // jwt를 헤더로 요청하면, 로그아웃이 가능하다.

        val principal = authentication.principal as UserDetails
        val userId = principal.username.toLong()

        refreshTokenRepository.deleteByUserId(userId)
    }

}