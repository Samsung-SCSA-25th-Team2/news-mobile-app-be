package com.example.news.article.exception

/**
 * 기사를 찾을 수 없을 때 발생하는 예외
 */
class ArticleNotFoundException(message: String) : RuntimeException(message)
