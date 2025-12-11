package com.example.news.user.service

import com.example.news.bookmark.repository.BookmarkRepository
import com.example.news.user.dto.UserResponse
import com.example.news.user.dto.mapper.toResponse
import com.example.news.user.exception.UserNotFoundException
import com.example.news.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val bookmarkRepository: BookmarkRepository
) {

    fun getUserById(userId: Long) : UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("해당 유저가 존재하지 않습니다.") }

        // N+1 문제 방지: bookmarkCount를 별도 쿼리로 조회
        val bookmarkCount = userRepository.countBookmarksByUserId(userId)

        return user.toResponse(bookmarkCount)
    }

    @Transactional
    fun deleteUser(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException("해당 유저가 존재하지 않습니다.")
        }

        // 1. 먼저 해당 유저의 모든 북마크 삭제 (양방향 관계 제거로 수동 처리 필요)
        bookmarkRepository.deleteAllByUserId(userId)

        // 2. 유저 삭제
        userRepository.deleteById(userId)
    }

}
