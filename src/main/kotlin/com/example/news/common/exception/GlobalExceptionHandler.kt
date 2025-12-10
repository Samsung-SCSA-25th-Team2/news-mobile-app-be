package com.example.news.common.exception

import com.example.news.common.dto.ErrorResponse
import com.example.news.user.exception.UserNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 * 모든 컨트롤러에서 발생하는 예외를 일괄 처리합니다.
 *
 * - 401 예시
 *  1. AuthService에서 UnauthorizedException 발생
 *  2. `GlobalExceptionHandler`가 자동으로 catch
 *  3. HTTP 401 Unauthorized 응답 반환
 *  4. 응답 body: {"message": "에러 메시지", "timestamp": "2025-12-10T..."}
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * UnauthorizedException -> 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(message = e.message))
    }

    /**
     * UserNotFoundException -> 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(message = e.message))
    }

    /**
     * 기타 예외 -> 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(message = "서버 오류가 발생했습니다."))
    }
}
