package com.example.news.common.dto.pagination

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "페이징 응답")
data class PageResponse<T>(
    @Schema(description = "현재 페이지의 데이터 목록")
    val content: List<T>,

    @Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,

    @Schema(description = "페이지 크기", example = "20")
    val size: Int,

    @Schema(description = "전체 데이터 개수", example = "100")
    val totalElements: Long,

    @Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,

    @Schema(description = "마지막 페이지 여부", example = "false")
    val last: Boolean
)
