package com.example.gava.domain.auth.service

import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.CustomException
import com.example.gava.security.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
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
        // 1. 사용자 인증
        val authenticationToken = UsernamePasswordAuthenticationToken(username, password)
        val authentication = authenticationManager.authenticate(authenticationToken)

        // 2. 인증 성공 시 JWT 토큰 생성
        val userDetails = authentication.principal as UserDetails
        val roles = userDetails.authorities.map { it.authority }

        val accessToken = jwtTokenProvider.createToken(userDetails.username, roles)
        val refreshToken = jwtTokenProvider.createRefreshToken(userDetails.username)

        // 3. Refresh Token 저장
        val user: User = userRepository.findByUsername(username)
            ?: throw CustomException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "User not found")
        user.refreshToken = refreshToken

        // 4. 토큰 응답
        val tokenResponse = TokenResponse("Bearer", accessToken, refreshToken, jwtTokenProvider.getExpirationTime());

        return tokenResponse
    }

    fun refresh(refreshToken: String): TokenResponse {
        // 1. Refresh Token 유효성 검사
        val username = jwtTokenProvider.getUsername(refreshToken)

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException(HttpStatus.BAD_REQUEST, "INVALID_REFRESH_TOKEN", "Invalid Refresh Token")
        }

        val user: User = userRepository.findByUsername(username)
            ?: throw CustomException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "User not found")

        if (user.refreshToken != refreshToken) {
            throw CustomException(HttpStatus.BAD_REQUEST, "REFRESH_TOKEN_MISMATCH", "Refresh Token mismatch")
        }

        // 2. 새로운 Token 생성
        val roles = user.roles.toList()
        val newAccessToken = jwtTokenProvider.createToken(user.username, roles)
        val newRefreshToken = jwtTokenProvider.createRefreshToken(user.username)

        // 3. Refresh Token 갱신
        user.refreshToken = newRefreshToken

        // 4. 토큰 응답
        return TokenResponse("Bearer", newAccessToken, newRefreshToken, jwtTokenProvider.getExpirationTime())
    }
}
