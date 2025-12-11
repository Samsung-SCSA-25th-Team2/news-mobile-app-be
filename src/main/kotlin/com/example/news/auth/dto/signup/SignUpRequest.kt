package com.example.news.auth.dto.signup

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "회원가입 요청")
data class SignUpRequest(

    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    val email: String,

    @field:Size(min = 8, message = "비밀번호는 8자 이상")
    @Schema(description = "비밀번호 (최소 8자)", example = "password123!", requiredMode = Schema.RequiredMode.REQUIRED)
    val password: String,
)
