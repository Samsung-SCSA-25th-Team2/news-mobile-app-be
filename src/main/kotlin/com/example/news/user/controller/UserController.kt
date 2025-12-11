package com.example.news.user.controller

import com.example.news.common.dto.error.ErrorResponse
import com.example.news.user.dto.UserResponse
import com.example.news.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "사용자 API", description = "사용자 정보 조회 및 회원 탈퇴 관련 API")
@RestController
@RequestMapping("/api/v1/user")
class UserController(
    private val userService: UserService
) {

    @Operation(
        summary = "내 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다. JWT 토큰이 필요합니다.",
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "내 정보 조회 성공",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @GetMapping("/me")
    fun getMyInfo(
        @AuthenticationPrincipal principal: UserDetails
    ): ResponseEntity<UserResponse> {
        val userId = principal.username.toLong()
        val userResponse = userService.getUserById(userId)
        return ResponseEntity.ok(userResponse)
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "특정 사용자를 탈퇴 처리합니다. 해당 사용자의 모든 북마크도 함께 삭제됩니다.",
        security = [SecurityRequirement(name = "JWT Authorization")]
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "회원 탈퇴 성공"
            ),
            ApiResponse(
                responseCode = "401",
                description = "인증 실패 (토큰 없음 또는 유효하지 않음)",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "사용자를 찾을 수 없음",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]
            )
        ]
    )
    @DeleteMapping("/{userId}")
    fun deleteUser(
        @Parameter(description = "탈퇴할 사용자 ID", example = "1")
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        userService.deleteUser(userId)
        return ResponseEntity.ok().build()
    }

}