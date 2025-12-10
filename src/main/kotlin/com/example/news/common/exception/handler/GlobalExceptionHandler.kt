package com.example.news.common.exception.handler

import com.example.news.auth.exception.DuplicateEmailException
import com.example.news.common.dto.ErrorResponse
import com.example.news.common.exception.UnauthorizedException
import com.example.news.user.exception.UserNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
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

    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * UnauthorizedException -> 401 Unauthorized
     */
    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        logger.warn("비인가 접근 - 401: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(message = e.message))
    }

    /**
     * UserNotFoundException -> 404 Not Found
     */
    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("사람을 찾을 수 없음 - 404: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(message = e.message))
    }

    /**
     * DuplicateEmailException -> 409 Conflict
     */
    @ExceptionHandler(DuplicateEmailException::class)
    fun handleDuplicateEmail(e: DuplicateEmailException): ResponseEntity<ErrorResponse> {
        logger.warn("이메일 중복 접근 - 409: {}", e.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ErrorResponse(message = e.message))
    }

    /**
     * MethodArgumentNotValidException -> 400 Bad Request
     * @Valid 검증 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMessage = e.bindingResult.fieldErrors
            .joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        logger.warn("검증 실패 - 400: {}", errorMessage)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(message = errorMessage))
    }

    /**
     * ConstraintViolationException -> 400 Bad Request
     * @Validated 검증 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val errorMessage = e.constraintViolations
            .joinToString(", ") { "${it.propertyPath}: ${it.message}" }
        logger.warn("제약조건 위반 - 400: {}", errorMessage)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(message = errorMessage))
    }

    /**
     * 기타 예외 -> 500 Internal Server Error
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("서버 오류 발생 - 500", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(message = "서버 오류가 발생했습니다."))
    }

}
