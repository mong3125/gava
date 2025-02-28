package com.example.gava.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class RefreshRequest(
    @field:NotBlank(message = "리프레시 토큰은 필수 입력값입니다")
    val refreshToken: String
)