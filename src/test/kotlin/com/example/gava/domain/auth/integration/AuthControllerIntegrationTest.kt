package com.example.gava.domain.auth.integration

import com.example.gava.domain.auth.dto.LoginRequest
import com.example.gava.domain.auth.dto.RefreshTokenRequest
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.ErrorCode
import com.example.gava.security.JwtTokenProvider
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val objectMapper = jacksonObjectMapper()

    private val testUsername = "testuser"
    private val testPassword = "testpassword"
    private val testRoles = setOf("ROLE_USER")

    @BeforeEach
    fun setup() {
        // 테스트 사용자 생성
        userRepository.save(
            User(
                username = testUsername,
                password = passwordEncoder.encode(testPassword),
                roles = testRoles
            )
        )
    }

    @Test
    fun `로그인 성공`() {
        // Given
        val request = LoginRequest(testUsername, testPassword)

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isOk() }
            jsonPath("$.tokenType") { value("Bearer") }
            jsonPath("$.accessToken") { exists() }
            jsonPath("$.refreshToken") { exists() }
            jsonPath("$.expirationTime") { exists() }
        }
    }

    @Test
    fun `로그인 실패 - user not found`() {
        // Given
        val request = LoginRequest("nonexistent", testPassword)

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_CREDENTIALS.name) }
        }
    }

    @Test
    fun `로그인 실패 - invalid password`() {
        // Given
        val request = LoginRequest(testUsername, "wrongpassword")

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_CREDENTIALS.name) }
        }
    }

    @Test
    fun `로그인 실패 - empty username`() {
        // Given
        val request = LoginRequest("", testPassword)

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.VALIDATION_FAILED.name) }
        }
    }

    @Test
    fun `로그인 실패 - empty password`() {
        // Given
        val request = LoginRequest(testUsername, "")

        // When & Then
        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.VALIDATION_FAILED.name) }
        }
    }

    @Test
    fun `refresh 성공 - success`() {
        // Given
        val loginResponse = performLogin()
        val refreshToken = loginResponse.refreshToken
        val refreshTokenRequest = RefreshTokenRequest(refreshToken)

        // When & Then
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshTokenRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { exists() }
            jsonPath("$.refreshToken") { exists() }
        }
    }

    @Test
    fun `refresh 실패 - invalid request`() {
        // Given
        val invalidToken = "invalid.token.here"

        // When & Then
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(invalidToken)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.VALIDATION_FAILED.name) }
        }
    }

    @Test
    fun `refresh 실패 - expired token`() {
        // Given
        val expiredToken = jwtTokenProvider.createRefreshToken(testUsername).apply {
            // 토큰을 강제로 만료시킴 (테스트용)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -1)
            jwtTokenProvider.getExpirationTime()
        }
        val refreshTokenRequest = RefreshTokenRequest(expiredToken)

        // When & Then
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshTokenRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_REFRESH_TOKEN.name) }
        }
    }

    @Test
    fun `refresh 실패 - token mismatch`() {
        // Given
        val loginResponse = performLogin()
        val refreshToken = loginResponse.refreshToken
        val refreshTokenRequest = RefreshTokenRequest(refreshToken)
        userRepository.findByUsernameWithRoles(testUsername)?.refreshToken = "modified.token"

        // When & Then
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(refreshTokenRequest)
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value(ErrorCode.INVALID_REFRESH_TOKEN.name) }
        }
    }

    @Test
    fun `refresh 실패 - empty token`() {
        // Given
        val emptyToken = ""
        val refreshTokenRequest = RefreshTokenRequest(emptyToken)

        // When & Then
        mockMvc.post("/api/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(emptyToken)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errorCode") { value(ErrorCode.VALIDATION_FAILED.name) }
        }
    }

    @Test
    fun `token expiration validation`() {
        // Given
        val loginResponse = performLogin()
        val accessToken = loginResponse.accessToken

        // When
        val isValid = jwtTokenProvider.validateToken(accessToken)

        // Then
        assertTrue(isValid)
    }

    @Test
    fun `refresh token persistence check`() {
        // Given
        val loginResponse = performLogin()

        // When
        val user = userRepository.findByUsernameWithRoles(testUsername)

        // Then
        assertNotNull(user?.refreshToken)
        assertEquals(loginResponse.refreshToken, user?.refreshToken)
    }

    @Test
    fun `role inclusion in token`() {
        // Given
        val loginResponse = performLogin()
        val accessToken = loginResponse.accessToken

        // When
        val roles = jwtTokenProvider.getRoles(accessToken)

        // Then
        assertTrue(roles.containsAll(testRoles))
    }

    private fun performLogin(): TokenResponse {
        val request = LoginRequest(testUsername, testPassword)
        val response = mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andReturn().response.contentAsString

        return objectMapper.readValue(response, TokenResponse::class.java)
    }
}