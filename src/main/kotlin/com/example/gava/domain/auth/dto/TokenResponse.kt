package com.example.gava.domain.auth.dto

data class TokenResponse(
    val tokenType: String = "Bearer",
    val accessToken: String,
    val refreshToken: String,
    val expirationTime: Long
)