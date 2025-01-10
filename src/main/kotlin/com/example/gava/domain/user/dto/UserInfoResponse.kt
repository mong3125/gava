package com.example.gava.domain.user.dto

data class UserInfoResponse(
    val id: Long?,
    val username: String,
    val roles: Set<String>,
)