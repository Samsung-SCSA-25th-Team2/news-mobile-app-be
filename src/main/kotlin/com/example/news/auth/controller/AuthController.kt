package com.example.news.auth.controller

import com.example.news.auth.dto.jwt.ReissueRequest
import com.example.news.auth.dto.jwt.TokenResponse
import com.example.news.auth.dto.login.LoginRequest
import com.example.news.auth.dto.signup.SignUpRequest
import com.example.news.auth.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<Unit> {
        authService.signup(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/reissue")
    fun reissue(@Valid @RequestBody request: ReissueRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.reissue(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<Unit> {
        // jwt를 헤더로 요청하면, 로그아웃이 가능하다.

        val userId = principal.username.toLong()
        authService.logout(userId)
        return ResponseEntity.ok().build()
    }

}