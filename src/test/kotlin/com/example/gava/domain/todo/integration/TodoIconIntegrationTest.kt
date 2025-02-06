package com.example.gava.domain.todo.integration

import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TodoIconIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        // 샘플 파일 내용 및 API 기본 경로 상수
        private val sampleFileContent = "test-image-content".toByteArray()
        private const val API_TODO_ICONS = "/api/todo-icons"
    }

    /**
     * 테스트에 사용될 이미지 파일을 생성하는 헬퍼 함수.
     *
     * @param filename 생성할 파일 이름
     * @param content 파일 내용
     * @return 생성된 MockMultipartFile
     */
    private fun createMockImageFile(filename: String, content: ByteArray): MockMultipartFile {
        return MockMultipartFile(
            "file",
            filename,
            MediaType.IMAGE_PNG_VALUE,
            content
        )
    }

    /**
     * 아이콘을 생성하는 헬퍼 함수.
     *
     * @param iconName 생성할 아이콘의 이름 (기본값: "Test Icon")
     * @return 생성된 아이콘의 ID
     */
    private fun createTestIcon(iconName: String = "Test Icon"): Int {
        val multipartFile = createMockImageFile("test.png", sampleFileContent)

        val result = mockMvc.perform(
            multipart(API_TODO_ICONS)
                .file(multipartFile)
                .param("name", iconName)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", equalTo(iconName)))
            .andReturn()

        return JsonPath.read(result.response.contentAsString, "$.id") as Int
    }

    @Test
    @DisplayName("아이콘 생성에 성공한다 (ADMIN 전용)")
    fun `아이콘 생성 성공`() {
        // given: 관리자 권한으로 아이콘 생성 요청을 위한 파일 및 파라미터 준비
        val iconName = "Test Icon"
        val multipartFile = createMockImageFile("test.png", sampleFileContent)

        // when & then: 요청 실행 및 결과 검증
        mockMvc.perform(
            multipart(API_TODO_ICONS)
                .file(multipartFile)
                .param("name", iconName)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(user("admin").roles("ADMIN"))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.name", equalTo(iconName)))
    }

    @Test
    @DisplayName("아이콘 생성에 실패한다 (ADMIN이 아닌 사용자)")
    fun `아이콘 생성 실패 - 권한 없음`() {
        // given: 일반 사용자로 요청 시도
        val multipartFile = createMockImageFile("test.png", sampleFileContent)

        // when & then: 권한 부족으로 요청 실패를 확인
        mockMvc.perform(
            multipart(API_TODO_ICONS)
                .file(multipartFile)
                .param("name", "Test Icon")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .with(user("user").roles("USER"))
        )
            .andExpect(status().isForbidden)
    }

    @Test
    @DisplayName("아이콘 단건 조회에 성공한다 (인증된 사용자)")
    fun `아이콘 단건 조회 성공`() {
        // given: 테스트 아이콘 생성
        val createdIconId = createTestIcon()

        // when & then: 생성한 아이콘을 단건 조회 후 상세 정보를 확인
        mockMvc.perform(
            get("$API_TODO_ICONS/{id}", createdIconId)
                .with(user("user").roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", equalTo(createdIconId)))
            .andExpect(jsonPath("$.name", equalTo("Test Icon")))
    }

    @Test
    @DisplayName("전체 아이콘 조회에 성공한다 (인증된 사용자)")
    fun `전체 아이콘 조회 성공`() {
        // given: 조회를 위해 아이콘을 하나 생성
        createTestIcon()

        // when & then: 전체 아이콘 목록을 조회 후 응답이 배열임을 확인
        mockMvc.perform(
            get(API_TODO_ICONS)
                .with(user("user").roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @DisplayName("아이콘 데이터 조회에 성공한다 (인증된 사용자)")
    fun `아이콘 데이터 조회 성공`() {
        // given: 테스트 아이콘 생성
        val createdIconId = createTestIcon()

        // when: 아이콘의 이미지 데이터를 조회
        val result = mockMvc.perform(
            get("$API_TODO_ICONS/{id}/data", createdIconId)
                .with(user("user").roles("USER"))
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
            .andExpect(header().longValue("Content-Length", sampleFileContent.size.toLong()))
            .andReturn()

        // then: 응답 받은 바이너리 데이터가 예상 데이터와 동일한지 확인
        assertArrayEquals(sampleFileContent, result.response.contentAsByteArray, "아이콘 데이터는 샘플 내용과 동일해야 합니다.")
    }
}
