package com.example.gava.domain.auth.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class SignupRequest(
    @field:Pattern(
        regexp = "^[a-zA-Z0-9가-힣]*$",
        message = "사용자 이름은 영문, 숫자, 한글만 가능합니다"
    )
    @field:NotBlank(message = "사용자 이름은 필수 항목입니다.")
    val username: String,

    @field:Pattern(
        regexp = "^[a-zA-Z0-9!@#\$%^&*()_+\\-=\\[\\]{};':\",.<>?/]*$",
        message = "비밀번호는 영문, 숫자만 가능합니다"
    )
    @field:NotBlank(message = "비밀번호는 필수 항목입니다.")
    val password: String
)