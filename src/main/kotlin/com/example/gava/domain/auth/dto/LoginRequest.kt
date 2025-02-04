package com.example.gava.domain.auth.dto

import jakarta.validation.constraints.NotBlank

data class LoginRequest(
    @field:NotBlank(message = "사용자 이름은 필수 항목입니다.")
    val username: String,

    @field:NotBlank(message = "비밀번호는 필수 항목입니다.")
    val password: String
)