package com.example.gava.domain.todo.integration

import com.jayway.jsonpath.JsonPath
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)  // 인스턴스 생명주기를 클래스 단위로 설정
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TodoIconIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    companion object {
        // 테스트에 사용할 샘플 이미지 파일 내용 (바이트 배열)
        val sampleFileContent = "test-image-content".toByteArray()
    }

    var createdIconId: Int = 0

    @Test
    @Order(1)
    @WithMockUser(username = "admin", roles = ["ADMIN"])
    fun `POST api todo-icons - 아이콘 생성`() {
        // MultipartFile 생성: name과 file 파라미터 전달
        val multipartFile = MockMultipartFile(
            "file",
            "test.png",
            MediaType.IMAGE_PNG_VALUE,
            sampleFileContent
        )

        // multipart/form-data 요청 전송
        val result = mockMvc.perform(
            multipart("/api/todo-icons")
                .file(multipartFile)
                .param("name", "Test Icon")
                .contentType(MediaType.MULTIPART_FORM_DATA)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // 응답에 name이 포함되어 있는지 확인
            .andExpect(jsonPath("$.name", equalTo("Test Icon")))
            .andReturn()

        // 생성된 아이콘의 id 추출 (응답 JSON에서 id 값을 읽어 저장)
        val responseBody = result.response.contentAsString
        createdIconId = JsonPath.read(responseBody, "$.id") as Int
        // id 값이 0보다 큰지 검증
        assert(createdIconId > 0)
    }

    @Test
    @Order(2)
    @WithMockUser(username = "user", roles = ["USER"])
    fun `GET api todo-icons - 아이콘 단건 조회`() {
        // 이전 테스트에서 생성된 아이콘 id를 사용하여 조회
        mockMvc.perform(get("/api/todo-icons/{id}", createdIconId))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", equalTo(createdIconId.toInt())))
            .andExpect(jsonPath("$.name", equalTo("Test Icon")))
    }

    @Test
    @Order(3)
    @WithMockUser(username = "user", roles = ["USER"])
    fun `GET api todo-icons - 전체 아이콘 조회`() {
        mockMvc.perform(get("/api/todo-icons"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            // 전체 응답이 리스트 형태인지 확인
            .andExpect(jsonPath("$").isArray)
    }

    @Test
    @Order(4)
    @WithMockUser(username = "user", roles = ["USER"])
    fun `GET api todo-icons data - 아이콘 데이터 조회`() {
        // 이미지 데이터 조회 API 테스트
        val result = mockMvc.perform(get("/api/todo-icons/{id}/data", createdIconId))
            .andExpect(status().isOk)
            // 응답 헤더에 content-type과 content-length가 올바른지 확인
            .andExpect(header().string("Content-Type", MediaType.IMAGE_PNG_VALUE))
            .andExpect(header().longValue("Content-Length", sampleFileContent.size.toLong()))
            .andReturn()

        // 실제 응답 바이트 배열이 테스트 파일과 동일한지 검증
        val responseBytes = result.response.contentAsByteArray
        assert(responseBytes.contentEquals(sampleFileContent))
    }
}
