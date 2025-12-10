package com.example.news.user.exception

/**
 * 유저를 찾지 못하는 경우
 * HTTP 404 NotFound 응답으로 변환됩니다.
 */
class UserNotFoundException(
    message: String
) : RuntimeException(message)
