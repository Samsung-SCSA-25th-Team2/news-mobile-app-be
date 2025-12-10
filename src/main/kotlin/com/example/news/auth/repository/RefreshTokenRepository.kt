package com.example.news.auth.repository

import com.example.news.auth.domain.RefreshToken
import org.springframework.data.repository.CrudRepository

/**
 * CrudRepository <엔티티, ID타입>
 *
 * - RefreshToken 엔티티를 Redis에 저장/조회/삭제하는 역할을 수행하는
 * - Spring Data Redis Repository
 */
interface RefreshTokenRepository : CrudRepository<RefreshToken, Long> {

    // userId 칼럼을 조건으로 RefreshToken 엔티티 하나를 찾아라
    // RefreshToken? (nullable)
    // - 있으면: RefreshToken
    // - 없으면: null
    fun findByUserId(userId: Long): RefreshToken?

    // userId 칼럼을 조건으로 RefreshToken 엔티티를 삭제해라
    fun deleteByUserId(userId: Long?)

}
