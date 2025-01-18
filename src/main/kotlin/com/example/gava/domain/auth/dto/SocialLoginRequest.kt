package com.example.gava.domain.auth.dto

data class SocialLoginRequest(
    val provider: String, // "google", "kakao", "apple", "naver" 
    val token: String     // idToken 또는 accessToken
)