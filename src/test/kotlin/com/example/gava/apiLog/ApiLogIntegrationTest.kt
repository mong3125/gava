package com.example.gava.apiLog

import com.example.gava.apiLog.data.ApiLogEntity
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.CustomUserDetails
import com.example.gava.util.TestSecurityUtils.createTestUser
import com.example.gava.util.TestSecurityUtils.toCustomUserDetails
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * ApiLogInterceptor 및 관련 컴포넌트의 통합 테스트
 *
 * - TestController: 테스트용 엔드포인트 제공 (정상 응답, 에러 응답)
 * - MockMvc를 통한 HTTP 요청/응답 검증 및 ApiLog 정보 DB 저장 확인
 * - 배치 임계치 도달 시 자동 flush 검증
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(ApiLogIntegrationTest.TestController::class)
@Transactional
class ApiLogIntegrationTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var apiLogRepository: ApiLogRepository

    @Autowired
    private lateinit var apiLogService: ApiLogService

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user1Details: CustomUserDetails
    private lateinit var user2Details: CustomUserDetails

    @BeforeEach
    fun setup() {
        // 로그 저장소 및 사용자 저장소 초기화
        apiLogRepository.deleteAll()
        userRepository.deleteAll()
        // 테스트용 사용자 생성 (예: user1, user2)
        user1 = createTestUser(userRepository, "user1")
        user2 = createTestUser(userRepository, "user2")
        user1Details = toCustomUserDetails(user1)
        user2Details = toCustomUserDetails(user2)
    }

    /**
     * 테스트용 컨트롤러
     * - /api/test/success : 정상 응답 ("success") 반환
     * - /api/test/error   : RuntimeException 발생하여 500 에러 반환
     */
    @RestController
    @RequestMapping("/api/test")
    class TestController {
        @GetMapping("/success")
        fun success(@RequestParam(required = false) param: String?): String {
            return "success"
        }

        @GetMapping("/error")
        fun error(): String {
            throw RuntimeException("Test exception")
        }
    }

    /**
     * [GET /api/test/success] 정상 요청 시 ApiLog가 올바르게 기록되는지 검증.
     *
     * 조건:
     * - 인증된 사용자(사용자 ID: 123)로 요청
     * - User-Agent, X-Forwarded-For 헤더가 포함됨
     */
    @Test
    fun `logApi - 성공 요청 정상 로그 생성`() {
        // When: GET /api/test/success 요청 (헤더 설정)
        mockMvc.perform(
            get("/api/test/success")
                .header("User-Agent", "TestAgent")
                .header("X-Forwarded-For", "192.168.0.1")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("success"))

        // 로그 비동기 적재로 인한 DB 반영을 위해 flush 호출
        apiLogService.flushLogsToDb()

        // Then: DB에 생성된 ApiLog 검증
        val logs: List<ApiLogEntity> = apiLogRepository.findAll()
        assertEquals(1, logs.size)

        val log = logs[0]
        assertEquals("/api/test/success", log.requestUri)
        assertEquals("GET", log.method)
        assertTrue(log.requestParam.isEmpty())
        assertEquals(200, log.status)
        assertNotNull(log.timestamp)
        assertTrue(log.executionTime >= 0)
        assertEquals(user1.id, log.userId)
        assertEquals("192.168.0.1", log.clientIp)
        assertEquals("TestAgent", log.userAgent)
        assertEquals(null, log.errorMessage)
    }

    /**
     * [GET /api/test/error] 에러 요청 시 ApiLog에 에러 메시지가 기록되는지 검증.
     *
     * 조건:
     * - 인증정보 없이 요청
     * - User-Agent, X-Forwarded-For 헤더가 포함됨
     * - RuntimeException("Test exception") 발생 → 500 에러 응답
     */
    @Test
    fun `logApi - 에러 요청 로그 생성`() {
        // When: GET /api/test/error 요청 (에러 발생)
        mockMvc.perform(
            get("/api/test/error")
                .header("User-Agent", "ErrorAgent")
                .header("X-Forwarded-For", "10.0.0.1")
                .with(user(user1Details))
        )
            .andExpect(status().isInternalServerError)

        // 로그 강제 flush
        apiLogService.flushLogsToDb()

        // Then: DB에 생성된 ApiLog 검증 (에러 메시지 포함)
        val logs = apiLogRepository.findAll()
        assertEquals(1, logs.size)

        val log = logs[0]
        assertEquals("/api/test/error", log.requestUri)
        assertEquals("GET", log.method)
        assertTrue(log.requestParam.isEmpty())
        assertEquals(500, log.status)
        assertNotNull(log.timestamp)
        assertTrue(log.executionTime >= 0)
        assertEquals(user1.id, log.userId)
        assertEquals("10.0.0.1", log.clientIp)
        assertEquals("ErrorAgent", log.userAgent)
        assertNotNull(log.errorMessage)
        assertTrue(log.errorMessage!!.contains("RuntimeException"))
        assertTrue(log.errorMessage!!.contains("Test exception"))
    }

    /**
     * [GET /api/test/success] 쿼리 파라미터가 포함된 요청 시,
     * ApiLog에 올바른 쿼리 문자열이 기록되는지 검증.
     */
    @Test
    fun `logApi - 쿼리 파라미터 로그 생성`() {
        // When: GET 요청 with query parameters
        mockMvc.perform(
            get("/api/test/success")
                .queryParam("param1", "value1")
                .queryParam("param2", "value2")
                .header("User-Agent", "QueryAgent")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().string("success"))

        // 로그 flush
        apiLogService.flushLogsToDb()

        // Then: requestParam에 쿼리 문자열이 올바르게 기록되었는지 검증
        val logs = apiLogRepository.findAll()
        assertEquals(1, logs.size)

        val log = logs[0]
        // 쿼리 문자열은 "param1=value1&param2=value2" 형식(순서는 상관없음)
        val params = log.requestParam.split("&").sorted()
        assertEquals(listOf("param1=value1", "param2=value2"), params)
    }
}
