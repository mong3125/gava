package com.example.gava.domain.auth.service

import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import com.example.gava.security.JwtTokenProvider
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val authenticationManager: AuthenticationManager,
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) {

    fun login(username: String, password: String): TokenResponse {
        return try {
            // 인증 시도
            val authenticationToken = UsernamePasswordAuthenticationToken(username, password)
            authenticationManager.authenticate(authenticationToken)

            // 인증 성공 시 토큰 발급 (사용자 조회 로직 제거)
            val user = userRepository.findByUsernameWithRoles(username)
                ?: throw CustomException(ErrorCode.USER_NOT_FOUND, "$username not found")

            // 토큰 발급
            generateToken(user)

        } catch (e: AuthenticationException) {
            throw CustomException(ErrorCode.INVALID_CREDENTIALS, "사용자 이름 또는 비밀번호가 일치하지 않습니다")
        }
    }


    fun refresh(refreshToken: String): TokenResponse {
        // Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "refreshToken is invalid")
        }

        val username = jwtTokenProvider.getUsername(refreshToken)

        val user: User = userRepository.findByUsernameWithRoles(username)
            ?: throw CustomException(ErrorCode.USER_NOT_FOUND, "$username is not found")

        if (user.refreshToken != refreshToken) {
            throw CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "$refreshToken is not matched to user")
        }

        // Token 재발급
        return generateToken(user)
    }

    fun generateToken(user: User): TokenResponse {
        // 사용자 ID
        val userId = user.id ?: throw CustomException(ErrorCode.INTERNAL_SERVER_ERROR, "User ID is null")

        // Token 발급
        val accessToken = jwtTokenProvider.createToken(userId, user.username, user.roles.toList())
        val refreshToken = jwtTokenProvider.createRefreshToken(user.username)
        val expirationTime = jwtTokenProvider.getExpirationTime()

        // Refresh Token 저장
        user.refreshToken = refreshToken

        // Token 반환
        return TokenResponse(
            tokenType = "Bearer",
            accessToken = accessToken,
            refreshToken = refreshToken,
            expirationTime = expirationTime
        )
    }
}
