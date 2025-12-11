package com.example.news.auth.controller

import com.example.news.auth.dto.jwt.ReissueRequest
import com.example.news.auth.dto.jwt.TokenResponse
import com.example.news.auth.dto.login.LoginRequest
import com.example.news.auth.dto.signup.SignUpRequest
import com.example.news.auth.service.AuthService
import com.example.news.common.dto.error.ErrorResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 재발급, 로그아웃 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @Operation(
        summary = "회원가입",
        description = "새로운 사용자를 등록합니다. 이메일과 비밀번호를 입력받아 회원가입을 처리합니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원가입 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (유효성 검증 실패)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "409",
                description = "이메일 중복",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<Unit> {
        authService.signup(request)
        return ResponseEntity.ok().build()
    }

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인하여 AccessToken과 RefreshToken을 발급받습니다."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그인 성공, 토큰 발급 완료",
                content = [Content(schema = Schema(implementation = TokenResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (이메일 또는 비밀번호 불일치)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @Operation(
        summary = "토큰 재발급",
        description = "RefreshToken을 사용하여 새로운 AccessToken과 RefreshToken을 발급받습니다. (Refresh Token Rotation 적용)"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "토큰 재발급 성공",
                content = [Content(schema = Schema(implementation = TokenResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "RefreshToken이 유효하지 않거나 만료됨",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/reissue")
    fun reissue(@Valid @RequestBody request: ReissueRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.reissue(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 로그인된 사용자를 로그아웃합니다. Redis에 저장된 RefreshToken을 삭제합니다.",
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "로그아웃 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {
        val userId = principal.username.toLong()
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }

}