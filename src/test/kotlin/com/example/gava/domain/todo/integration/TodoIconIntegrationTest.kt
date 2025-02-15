package com.example.gava.domain.todo.integration

import com.example.gava.domain.todo.entity.Icon
import com.example.gava.domain.todo.repository.TodoIconRepository
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TodoIconIntegrationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var todoIconRepository: TodoIconRepository

    @BeforeEach
    fun setup() {
        // 테스트마다 repository 초기화
        todoIconRepository.deleteAll()
    }

    /**
     * [POST /api/todo-icons] 정상적인 아이콘 생성 테스트.
     * - 관리자 권한(@WithMockUser(roles = ["ADMIN"]))이 있어야 하며,
     *   파일 업로드 및 name 파라미터가 올바르게 전달된 경우 응답이 정상적으로 반환되는지 검증.
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `createIcon - 정상 동작 케이스`() {
        val fileContent = "Test Image Content".toByteArray()
        val multipartFile = MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            fileContent
        )

        mockMvc.perform(
            multipart("/api/todo-icons")
                .file(multipartFile)
                .param("name", "테스트 아이콘")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("테스트 아이콘"))
            .andExpect(jsonPath("$.contentType").value("image/png"))
    }

    /**
     * [POST /api/todo-icons] 파일이 비어있는 경우 예외 발생 테스트.
     */
    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `createIcon - 파일 비어있는 경우 예외 처리`() {
        val emptyFile = MockMultipartFile(
            "file",
            "empty.png",
            "image/png",
            ByteArray(0)
        )

        mockMvc.perform(
            multipart("/api/todo-icons")
                .file(emptyFile)
                .param("name", "빈 파일 테스트")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assertTrue(result.response.contentAsString.contains("업로드된 파일이 비어있습니다."))
            }
    }

    /**
     * [POST /api/todo-icons] 관리자 권한 없이 요청 시 403 Forbidden 반환 테스트.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `createIcon - 관리자 권한 없이 요청 시 403 Forbidden`() {
        val fileContent = "Test Image Content".toByteArray()
        val multipartFile = MockMultipartFile(
            "file",
            "test.png",
            "image/png",
            fileContent
        )

        // 관리자 권한 없이 요청
        mockMvc.perform(
            multipart("/api/todo-icons")
                .file(multipartFile)
                .param("name", "테스트 아이콘")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isForbidden)
    }

    /**
     * [GET /api/todo-icons/{id}] 아이콘 단건 조회 정상 동작 테스트.
     * - 미리 저장한 아이콘 정보를 기반으로 응답이 올바르게 반환되는지 검증.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getById - 정상 동작 케이스`() {
        // 테스트용 아이콘 저장
        val icon = Icon(
            name = "조회 테스트 아이콘",
            data = "DummyData".toByteArray(),
            contentType = "image/png"
        )
        val savedIcon = todoIconRepository.save(icon)

        mockMvc.perform(
            get("/api/todo-icons/{id}", savedIcon.id)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedIcon.id))
            .andExpect(jsonPath("$.name").value("조회 테스트 아이콘"))
            .andExpect(jsonPath("$.contentType").value("image/png"))
    }

    /**
     * [GET /api/todo-icons/{id}] 존재하지 않는 아이콘 아이디 조회 시 예외 발생 테스트.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getById - 존재하지 않는 아이디 경우 예외 처리`() {
        val nonExistentId = 999L

        mockMvc.perform(
            get("/api/todo-icons/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assertTrue(result.response.contentAsString.contains("Icon id: $nonExistentId 를 찾을 수 없습니다."))
            }
    }

    /**
     * [GET /api/todo-icons] 전체 아이콘 리스트 조회 테스트.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getAll - 전체 아이콘 리스트 조회`() {
        // 테스트용 아이콘 2개 저장
        val icon1 = Icon(
            name = "아이콘1",
            data = "Data1".toByteArray(),
            contentType = "image/png"
        )
        val icon2 = Icon(
            name = "아이콘2",
            data = "Data2".toByteArray(),
            contentType = "image/jpeg"
        )
        todoIconRepository.save(icon1)
        todoIconRepository.save(icon2)

        mockMvc.perform(
            get("/api/todo-icons")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
    }

    /**
     * [GET /api/todo-icons/{id}/data] 아이콘 데이터(바이트 배열) 조회 테스트.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getDataById - 정상 동작 케이스`() {
        val testData = "BinaryDataTest".toByteArray()
        val icon = Icon(
            name = "데이터 테스트 아이콘",
            data = testData,
            contentType = "image/gif"
        )
        val savedIcon = todoIconRepository.save(icon)

        mockMvc.perform(
            get("/api/todo-icons/{id}/data", savedIcon.id)
        )
            .andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "image/gif"))
            .andExpect(header().longValue("Content-Length", testData.size.toLong()))
            .andExpect { result ->
                val responseBytes = result.response.contentAsByteArray
                assertArrayEquals(testData, responseBytes)
            }
    }

    /**
     * [GET /api/todo-icons/{id}/data] 존재하지 않는 아이콘 데이터 조회 시 예외 발생 테스트.
     */
    @Test
    @WithMockUser(roles = ["USER"])
    fun `getDataById - 존재하지 않는 아이디 경우 예외 처리`() {
        val nonExistentId = 888L

        mockMvc.perform(
            get("/api/todo-icons/{id}/data", nonExistentId)
        )
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assertTrue(result.response.contentAsString.contains("Icon id: $nonExistentId 를 찾을 수 없습니다."))
            }
    }
}
