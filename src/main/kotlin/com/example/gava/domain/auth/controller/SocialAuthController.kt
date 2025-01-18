package com.example.gava.domain.auth.controller

import com.example.gava.domain.auth.dto.SocialLoginRequest
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.auth.service.AuthService
import com.example.gava.domain.auth.service.SocialAuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/social")
class SocialAuthController(
    private val socialAuthService: SocialAuthService,
    private val authService: AuthService
) {
    @PostMapping("/social/login")
    fun socialLogin(@RequestBody request: SocialLoginRequest): ResponseEntity<TokenResponse> {
        // 소셜 토큰 검증
        val socialUserInfo = socialAuthService.verifyAndGetUserInfo(
            provider = request.provider,
            token = request.token
        )

        // 사용자 조회 / 가입
        val user = socialAuthService.createOrGetUser(socialUserInfo)

        // JWT 토큰 발급
        val tokenResponse = authService.generateToken(user)

        return ResponseEntity.ok(tokenResponse)
    }
}