package com.example.news.article.service

import com.example.news.article.domain.Reaction
import com.example.news.article.domain.ReactionType
import com.example.news.article.exception.ArticleNotFoundException
import com.example.news.article.repository.ReactionRepository
import com.example.news.article.repository.ArticleRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReactionService(
    private val articleRepository: ArticleRepository,
    private val reactionRepository: ReactionRepository
) {

    /**
     * 좋아요/싫어요/해제까지 포함한 반응 설정
     * - type = LIKE     : 좋아요
     * - type = DISLIKE  : 싫어요
     * - type = NONE     : 반응 해제
     */
    @Transactional
    fun react(articleId: Long, userId: Long, type: ReactionType) {

        // 낙관적 락은 Article의 @Version으로 자동 처리됨
        val article = articleRepository.findById(articleId)
            .orElseThrow() { ArticleNotFoundException("해당 기사가 존재하지 않습니다.") }

        // 반응 꺼내기
        val existing: Reaction? =
            reactionRepository.findByArticleArticleIdAndUserId(articleId, userId)

        when {
            // 기존 반응 없음 + 새 반응이 LIKE or DISLIKE → 반응 추가
            existing == null && type != ReactionType.NONE -> {
                reactionRepository.save(
                    Reaction(
                        article = article,
                        userId = userId,
                        type = type
                    )
                )
                when (type) {
                    ReactionType.LIKE -> article.likes += 1
                    ReactionType.DISLIKE -> article.dislikes += 1
                    ReactionType.NONE -> {}
                }
            }

            // 기존 반응 있음 + 새 type = NONE → 반응 해제
            existing != null && type == ReactionType.NONE -> {
                when (existing.type) {
                    ReactionType.LIKE -> article.likes -= 1
                    ReactionType.DISLIKE -> article.dislikes -= 1
                    ReactionType.NONE -> {}
                }
                reactionRepository.delete(existing)
            }

            // 기존 반응 있음 + 새 type 이 다름 (LIKE ↔ DISLIKE 변경)
            existing != null && type != ReactionType.NONE && existing.type != type -> {
                when (existing.type) {
                    ReactionType.LIKE -> {
                        article.likes -= 1
                        article.dislikes += 1
                    }

                    ReactionType.DISLIKE -> {
                        article.dislikes -= 1
                        article.likes += 1
                    }

                    ReactionType.NONE -> {}
                }
                existing.type = type  // JPA 영속 상태라 save() 불필요
            }

            // 기존 반응 있음 + 새 type 이 같음 (멱등 요청) → 아무 것도 안 함
            else -> { /* do nothing */
            }
        }
    }
}
