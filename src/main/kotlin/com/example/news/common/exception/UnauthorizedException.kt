package com.example.news.common.exception

/**
 * 인증 실패 시 발생하는 예외
 * HTTP 401 Unauthorized 응답으로 변환됩니다.
 */
class UnauthorizedException(
    message: String
) : RuntimeException(message)
