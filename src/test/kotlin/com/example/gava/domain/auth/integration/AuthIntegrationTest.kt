package com.example.gava.domain.auth.integration

import com.example.gava.domain.auth.dto.LoginRequest
import com.example.gava.domain.auth.dto.RefreshRequest
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.JwtTokenProvider
import com.example.gava.util.TestSecurityUtils.createTestUser
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var entityManager: EntityManager

    private fun asJsonString(obj: Any): String = objectMapper.writeValueAsString(obj)

    @BeforeEach
    fun setup() {
        userRepository.deleteAll()
    }

    /**
     * [POST /api/auth/login] 정상적인 로그인 테스트.
     * - 올바른 사용자 이름과 비밀번호로 로그인 시 토큰이 반환되는지 확인.
     */
    @Test
    fun `login - 정상 동작 케이스`() {
        // Given: 사용자 생성
        val username = "testuser"
        val password = "password123"
        createTestUser(userRepository, username, passwordEncoder.encode(password))

        // When: 로그인 요청
        val loginRequest = LoginRequest(username, password)
        val jsonRequest = asJsonString(loginRequest)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 토큰이 성공적으로 반환됨
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expirationTime").isNumber())
    }

    /**
     * [POST /api/auth/login] 잘못된 비밀번호로 로그인 시도 시 예외 처리 테스트.
     * - 잘못된 비밀번호 입력 시 INVALID_CREDENTIALS 예외가 발생하는지 확인.
     */
    @Test
    fun `login - 잘못된 비밀번호로 시도 시 예외 처리`() {
        // Given: 사용자 생성
        val username = "testuser"
        val password = "password123"
        createTestUser(userRepository, username, passwordEncoder.encode(password))

        // When: 잘못된 비밀번호로 로그인 요청
        val wrongPassword = "wrongpassword"
        val loginRequest = LoginRequest(username, wrongPassword)
        val jsonRequest = asJsonString(loginRequest)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 401 Unauthorized 상태 및 JSON 형식 검증
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("사용자 이름 또는 비밀번호가 일치하지 않습니다"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    /**
     * [POST /api/auth/login] 존재하지 않는 사용자 이름으로 로그인 시도 시 예외 처리 테스트.
     * - 존재하지 않는 사용자 이름 입력 시 USER_NOT_FOUND 예외가 발생하는지 확인.
     */
    @Test
    fun `login - 존재하지 않는 사용자 이름으로 시도 시 예외 처리`() {
        // Given: 존재하지 않는 사용자 이름
        val nonExistentUsername = "nonexistent"
        val password = "password123"

        // When: 로그인 요청
        val loginRequest = LoginRequest(nonExistentUsername, password)
        val jsonRequest = asJsonString(loginRequest)

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 401 Unauthorized
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"))
            .andExpect(jsonPath("$.message").value("사용자 이름 또는 비밀번호가 일치하지 않습니다"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    /**
     * [POST /api/auth/refresh] 유효한 Refresh Token으로 토큰 재발급 테스트.
     * - 유효한 Refresh Token으로 요청 시 새로운 토큰이 발급되는지 확인.
     */
    @Test
    fun `refresh - 유효한 Refresh Token으로 재발급`() {
        // Given: 사용자 생성 및 로그인하여 Refresh Token 발급
        val username = "testuser"
        val password = "password123"
        createTestUser(userRepository, username, passwordEncoder.encode(password))
        val tokenResponse = loginAndGetTokenResponse(username, password)

        // When: Refresh Token으로 재발급 요청
        val refreshRequest = RefreshRequest(tokenResponse.refreshToken)
        val jsonRequest = asJsonString(refreshRequest)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 새로운 토큰이 성공적으로 반환됨
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.expirationTime").isNumber())
    }

    /**
     * [POST /api/auth/refresh] 유효하지 않은 Refresh Token으로 재발급 시도 시 예외 처리 테스트.
     * - 유효하지 않은 Refresh Token 입력 시 INVALID_REFRESH_TOKEN 예외가 발생하는지 확인.
     */
    @Test
    fun `refresh - 유효하지 않은 Refresh Token으로 시도 시 예외 처리`() {
        // Given: 유효하지 않은 Refresh Token
        val invalidRefreshToken = "invalid.refresh.token"

        // When: 재발급 요청
        val refreshRequest = RefreshRequest(invalidRefreshToken)
        val jsonRequest = asJsonString(refreshRequest)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 401 Unauthorized 상태 및 JSON 형식 검증
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
            .andExpect(jsonPath("$.message").value("refreshToken is invalid"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    /**
     * [POST /api/auth/refresh] 사용자와 일치하지 않는 Refresh Token으로 재발급 시도 시 예외 처리 테스트.
     * - 사용자와 Refresh Token이 매핑되지 않은 경우 INVALID_REFRESH_TOKEN 예외가 발생하는지 확인.
     */
    @Test
    fun `refresh - 사용자와 일치하지 않는 Refresh Token으로 시도 시 예외 처리`() {
        // Given: 사용자 생성 및 로그인 후 Refresh Token 발급
        val username1 = "user1"
        val password = "password123"
        val user = createTestUser(userRepository, username1, passwordEncoder.encode(password))
        val tokenResponse1 = loginAndGetTokenResponse(username1, password)  // user1의 토큰 발급
        Thread.sleep(1000)  // 토큰 발급 시간 차이를 위해 1초 대기
        val tokenResponse2 = loginAndGetTokenResponse(username1, password)  // user1의 토큰 재발급

        // When: 사용자의 이전 Refresh Token으로 재발급 요청
        val refreshRequest = RefreshRequest(tokenResponse1.refreshToken)
        val jsonRequest = asJsonString(refreshRequest)

        mockMvc.perform(
            post("/api/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            // Then: 401 Unauthorized 상태 및 JSON 형식 검증
            .andExpect(status().isUnauthorized)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.errorCode").value("INVALID_REFRESH_TOKEN"))
            .andExpect(jsonPath("$.message").value("${tokenResponse1.refreshToken} is not matched to user"))
            .andExpect(jsonPath("$.timestamp").exists())
    }

    /**
     * 로그인 후 TokenResponse를 반환하는 헬퍼 메서드.
     */
    private fun loginAndGetTokenResponse(username: String, password: String): TokenResponse {
        val loginRequest = LoginRequest(username, password)
        val jsonRequest = asJsonString(loginRequest)

        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
        )
            .andExpect(status().isOk)
            .andReturn()

        val content = result.response.contentAsString
        return objectMapper.readValue(content, TokenResponse::class.java)
    }
}