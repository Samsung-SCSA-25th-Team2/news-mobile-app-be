package com.example.news.user.service

import com.example.news.user.dto.UserResponse
import com.example.news.user.dto.mapper.toResponse
import com.example.news.user.exception.UserNotFoundException
import com.example.news.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {

    fun getUserById(userId: Long) : UserResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException("해당 유저가 존재하지 않습니다.") }

        return user.toResponse() // UserMapper 헬퍼함수 사용 : User -> UserResponse
    }

    @Transactional
    fun deleteUser(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException("해당 유저가 존재하지 않습니다.")
        }

        userRepository.deleteById(userId)
    }

}
