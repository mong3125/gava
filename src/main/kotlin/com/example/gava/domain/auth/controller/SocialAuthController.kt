package com.example.gava.domain.auth.controller

import com.example.gava.domain.auth.dto.SocialLoginRequest
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.auth.service.SocialAuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth/social")
class SocialAuthController(
    private val socialAuthService: SocialAuthService
) {
    @PostMapping("/login")
    fun socialLogin(
        @RequestBody request: SocialLoginRequest
    ): ResponseEntity<TokenResponse> {
        val tokenResponse = socialAuthService.loginWithSocial(request.provider, request.token)
        return ResponseEntity.ok(tokenResponse)
    }
}
