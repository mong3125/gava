package com.example.gava.domain.auth.dto

data class SocialUserInfo(
    val provider: String,
    val providerId: String, // 구글: sub / 카카오: id / 네이버: id / 애플: sub
    val email: String?
)