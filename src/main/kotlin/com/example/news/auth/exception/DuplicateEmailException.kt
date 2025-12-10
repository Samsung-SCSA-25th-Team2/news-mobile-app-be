package com.example.news.auth.exception

/**
 * 이메일 중복 시 발생하는 예외
 * HTTP 409 Conflict 응답으로 변환됩니다.
 */
class DuplicateEmailException(
    message: String
) : RuntimeException(message)