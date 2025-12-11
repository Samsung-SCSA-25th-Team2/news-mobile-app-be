package com.example.news.common.dto.error

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "에러 응답")
data class ErrorResponse(
    @Schema(description = "에러 메시지", example = "요청한 리소스를 찾을 수 없습니다.")
    val message: String?,

    @Schema(description = "에러 발생 시간", example = "2025-12-11T10:30:00")
    val timestamp: LocalDateTime = LocalDateTime.now()
)