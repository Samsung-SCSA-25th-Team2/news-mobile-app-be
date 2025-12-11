package com.example.news.user.controller

import com.example.news.user.dto.UserResponse
import com.example.news.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    /**
     * 내 정보 조회
     * - jwt만 있으면 된다.
     */
    @GetMapping("/me")
    fun getMyInfo(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<UserResponse> {

        val userId = principal.username.toLong()  // username = userId.toString() 구조니까

        val userResponse = userService.getUserById(userId)

        return ResponseEntity.ok(userResponse)
    }

    /**
     * 유저 정보 조회
     *
     * - 필요없다고 판단함.
     * - 나만의 개인 서비스
     */
//    @GetMapping("/{userId}")
//    fun getUserInfo(@PathVariable userId: Long): ResponseEntity<UserResponse> {
//        val userResponse = userService.getUserById(userId)
//        return ResponseEntity.ok(userResponse)
//    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: Long): ResponseEntity<Unit> {
        userService.deleteUser(userId)
        return ResponseEntity.ok().build()
    }

}