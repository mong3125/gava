package com.example.gava.domain.todo.integration

import com.example.gava.domain.todo.dto.CreateTodoGroupRequest
import com.example.gava.domain.todo.dto.TodoGroupResponse
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.CustomUserDetails
import com.example.gava.util.TestSecurityUtils.createTestUser
import com.example.gava.util.TestSecurityUtils.toCustomUserDetails
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TodoGroupControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    private fun createTodoGroup(userDetails: CustomUserDetails, request: CreateTodoGroupRequest): TodoGroupResponse {
        val result = mockMvc.perform(
            post("/api/todo-groups")
                .with(user(userDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", notNullValue()))
            .andReturn()
        return objectMapper.readValue(result.response.contentAsString, TodoGroupResponse::class.java)
    }

    @Test
    fun `TodoGroup 생성 - 성공`() {
        // Given
        val user = createTestUser(userRepository, "user1")
        val userDetails = toCustomUserDetails(user)
        val request = CreateTodoGroupRequest(name = "Work", color = "#FF0000")

        // When
        val response = createTodoGroup(userDetails, request)

        // Then
        with(response) {
            assert(id > 0)
            assert(name == "Work")
            assert(color == "#FF0000")
        }
    }

    @Test
    fun `TodoGroup 단건 조회 - 성공`() {
        // Given
        val user = createTestUser(userRepository, "user1")
        val userDetails = toCustomUserDetails(user)
        val createRequest = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val createdGroup = createTodoGroup(userDetails, createRequest)

        // When
        val getRequest = get("/api/todo-groups/{id}", createdGroup.id)
            .with(user(userDetails))

        // Then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(createdGroup.id.toInt()))
            .andExpect(jsonPath("$.name").value("Work"))
    }

    @Test
    fun `TodoGroup 단건 조회 - 존재하지 않는 id 404`() {
        // Given
        val user = createTestUser(userRepository, "user1")
        val userDetails = toCustomUserDetails(user)
        val nonExistentId = 9999L

        // When
        val getRequest = get("/api/todo-groups/{id}", nonExistentId)
            .with(user(userDetails))

        // Then
        mockMvc.perform(getRequest)
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `TodoGroup 단건 조회 - 타 사용자 접근시 403`() {
        // Given: user1이 그룹 생성, user2가 조회 시도
        val user1 = createTestUser(userRepository, "user1")
        val user2 = createTestUser(userRepository, "user2")
        val user1Details = toCustomUserDetails(user1)
        val user2Details = toCustomUserDetails(user2)
        val createRequest = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val createdGroup = createTodoGroup(user1Details, createRequest)

        // When: user2가 user1의 TodoGroup 조회 API 호출
        val getRequest = get("/api/todo-groups/{id}", createdGroup.id)
            .with(user(user2Details))

        // Then: 403 Forbidden 상태가 반환됨
        mockMvc.perform(getRequest)
            .andExpect(status().isForbidden)
    }

    @Test
    fun `TodoGroup 전체 조회 - 사용자별 필터링`() {
        // Given: user1이 그룹 생성, user2는 그룹 생성 없음
        val user1 = createTestUser(userRepository, "user1")
        val user2 = createTestUser(userRepository, "user2")
        val user1Details = toCustomUserDetails(user1)
        val user2Details = toCustomUserDetails(user2)
        createTodoGroup(user1Details, CreateTodoGroupRequest(name = "Work", color = "#FF0000"))

        // When & Then: user1 조회 시 그룹 1개가 반환됨
        mockMvc.perform(
            get("/api/todo-groups")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Int>(1)))

        // When & Then: user2 조회 시 빈 리스트가 반환됨
        mockMvc.perform(
            get("/api/todo-groups")
                .with(user(user2Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Int>(0)))
    }

    @Test
    fun `TodoGroup 수정 - 성공`() {
        // Given: user1이 그룹 생성
        val user = createTestUser(userRepository, "user1")
        val userDetails = toCustomUserDetails(user)
        val createRequest = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val createdGroup = createTodoGroup(userDetails, createRequest)

        // And: 수정할 데이터 준비
        val updateRequest = CreateTodoGroupRequest(name = "Personal", color = "#00FF00")

        // When: 수정 API 호출
        val putRequest = put("/api/todo-groups/{id}", createdGroup.id)
            .with(user(userDetails))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))

        // Then: 응답에 수정된 데이터가 반영됨
        mockMvc.perform(putRequest)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(createdGroup.id.toInt()))
            .andExpect(jsonPath("$.name").value("Personal"))
            .andExpect(jsonPath("$.color").value("#00FF00"))
    }

    @Test
    fun `TodoGroup 수정 - 타 사용자 접근시 403`() {
        // Given: user1이 그룹 생성, user2가 수정 시도
        val user1 = createTestUser(userRepository, "user1")
        val user2 = createTestUser(userRepository, "user2")
        val user1Details = toCustomUserDetails(user1)
        val user2Details = toCustomUserDetails(user2)
        val createRequest = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val createdGroup = createTodoGroup(user1Details, createRequest)

        // And: 수정 요청 데이터 준비
        val updateRequest = CreateTodoGroupRequest(name = "Hacker", color = "#000000")

        // When: user2가 수정 API 호출
        val putRequest = put("/api/todo-groups/{id}", createdGroup.id)
            .with(user(user2Details))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest))

        // Then: 403 Forbidden 상태가 반환됨
        mockMvc.perform(putRequest)
            .andExpect(status().isForbidden)
    }

    @Test
    fun `TodoGroup 삭제 - 성공`() {
        // Given: user1이 그룹 생성
        val user = createTestUser(userRepository, "user1")
        val userDetails = toCustomUserDetails(user)
        val createRequest = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val createdGroup = createTodoGroup(userDetails, createRequest)

        // When: user1이 삭제 API 호출
        val deleteRequest = delete("/api/todo-groups/{id}", createdGroup.id)
            .with(user(userDetails))
        mockMvc.perform(deleteRequest)
            .andExpect(status().isNoContent)

        // Then: 삭제 후 조회 시 404 Not Found 상태 반환
        mockMvc.perform(
            get("/api/todo-groups/{id}", createdGroup.id)
                .with(user(userDetails))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `TodoGroup 삭제 - 타 사용자 접근시 403`() {
        // Given: user1이 그룹 생성, user2가 삭제 시도
        val user1 = createTestUser(userRepository, "user1")
        val user2 = createTestUser(userRepository, "user2")
        val user1Details = toCustomUserDetails(user1)
        val user2Details = toCustomUserDetails(user2)
        val createRequest = CreateTodoGroupRequest(name = "Temp", color = "#123456")
        val createdGroup = createTodoGroup(user1Details, createRequest)

        // When: user2가 삭제 API 호출
        val deleteRequest = delete("/api/todo-groups/{id}", createdGroup.id)
            .with(user(user2Details))

        // Then: 403 Forbidden 상태 반환
        mockMvc.perform(deleteRequest)
            .andExpect(status().isForbidden)
    }
}
