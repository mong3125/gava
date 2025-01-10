package com.example.gava.domain.auth.controller

import com.example.gava.domain.auth.dto.LoginRequest
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.auth.service.AuthService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<TokenResponse> {
        val tokenResponse: TokenResponse = authService.login(request.username, request.password)
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/refresh")
    fun refresh(@RequestBody refreshToken: String): ResponseEntity<TokenResponse> {
        val tokenResponse: TokenResponse = authService.refresh(refreshToken)
        return ResponseEntity.ok(tokenResponse)
    }
}
