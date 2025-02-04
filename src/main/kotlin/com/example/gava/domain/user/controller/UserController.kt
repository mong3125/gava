package com.example.gava.domain.user.controller

import com.example.gava.domain.auth.dto.SignupRequest
import com.example.gava.domain.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/users")
class UserController (
    private val userService: UserService
) {
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: SignupRequest): ResponseEntity<Void> {
        userService.signup(request.username, request.password)
        return ResponseEntity.status(201).build()
    }
}