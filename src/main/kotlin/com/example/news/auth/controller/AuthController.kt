package com.example.news.auth.controller

import com.example.news.auth.dto.jwt.ReissueRequest
import com.example.news.auth.dto.jwt.TokenResponse
import com.example.news.auth.dto.login.LoginRequest
import com.example.news.auth.dto.signup.SignUpRequest
import com.example.news.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<Unit> {
        authService.signup(request)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.login(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/reissue")
    fun reissue(@RequestBody request: ReissueRequest): ResponseEntity<TokenResponse> {
        val tokenResponse = authService.reissue(request)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    fun logout(authentication: Authentication): ResponseEntity<Unit> {
        authService.logout(authentication)
        return ResponseEntity.ok().build()
    }

}