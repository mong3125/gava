package com.example.gava.domain.user.controller

import com.example.gava.domain.auth.dto.SignupRequest
import com.example.gava.domain.user.dto.UserInfoResponse
import com.example.gava.domain.user.service.UserService
import com.example.gava.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(@RequestBody request: SignupRequest): ResponseEntity<Void> {
        userService.signup(request.username, request.password)
        return ResponseEntity.status(201).build()
    }

    @GetMapping("/me")
    fun test(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<UserInfoResponse> {
        val userInfoResponse = userService.getCurrentUserDetails(userDetails.getUserId())
        return ResponseEntity.ok(userInfoResponse)
    }
}