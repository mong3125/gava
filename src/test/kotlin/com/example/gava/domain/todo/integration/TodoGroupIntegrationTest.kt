package com.example.gava.domain.todo.integration

import com.example.gava.domain.todo.dto.CreateTodoGroupRequest
import com.example.gava.domain.todo.entity.TodoGroup
import com.example.gava.domain.todo.repository.TodoGroupRepository
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.CustomUserDetails
import com.example.gava.util.TestSecurityUtils.createTestUser
import com.example.gava.util.TestSecurityUtils.toCustomUserDetails
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.BeforeEach
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
class TodoGroupIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var todoGroupRepository: TodoGroupRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user1 : User
    private lateinit var user2 : User
    private lateinit var user1Details : CustomUserDetails
    private lateinit var user2Details : CustomUserDetails

    private fun asJsonString(obj: Any): String = objectMapper.writeValueAsString(obj)

    @BeforeEach
    fun setup() {
        todoGroupRepository.deleteAll()
        userRepository.deleteAll()
        user1 = createTestUser(userRepository, "user1")
        user2 = createTestUser(userRepository, "user2")
        user1Details = toCustomUserDetails(user1)
        user2Details = toCustomUserDetails(user2)
    }

    /**
     * [POST /api/todo-groups] 정상적인 TodoGroup 생성 테스트.
     */
    @Test
    fun `createTodoGroup - 정상 동작 케이스`() {
        // Given: TodoGroup 생성 요청
        val request = CreateTodoGroupRequest(name = "Work", color = "#FF0000")
        val jsonRequest = asJsonString(request)

        // When: TodoGroup 생성 API 호출
        mockMvc.perform(
            post("/api/todo-groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(user(user1Details))
        )
            // Then: 생성된 TodoGroup이 반환됨
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Work"))
            .andExpect(jsonPath("$.color").value("#FF0000"))
    }

    /**
     * [GET /api/todo-groups/{id}] 단건 조회 정상 동작 테스트.
     */
    @Test
    fun `getTodoGroupById - 정상 동작 케이스`() {
        // Given: user1이 그룹 생성
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)

        // When: 조회 API 호출
        mockMvc.perform(
            get("/api/todo-groups/{id}", savedGroup.id)
                .accept(MediaType.APPLICATION_JSON)
                .with(user(user1Details))
        )
            // Then: 생성된 TodoGroup이 반환됨
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedGroup.id))
            .andExpect(jsonPath("$.name").value("Group1"))
            .andExpect(jsonPath("$.color").value("#000000"))
    }

    /**
     * [GET /api/todo-groups/{id}] 존재하지 않는 id 조회 시 예외 처리 테스트.
     */
    @Test
    fun `getTodoGroupById - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 존재하지 않는 id로 조회 시도
        val nonExistentId = 999L
        // When: 조회 시도
        mockMvc.perform(
            get("/api/todo-groups/{id}", nonExistentId)
                .accept(MediaType.APPLICATION_JSON)
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 반환
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId is not found"))
            }
    }

    /**
     * [GET /api/todo-groups/{id}] 권한 없는 TodoGroup 조회 시 예외 처리 테스트.
     */
    @Test
    fun `getTodoGroupById - 권한 없는 경우 예외 처리`() {
        // Given: user1이 그룹 생성
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)

        // When: user2가 조회 시도
        mockMvc.perform(
            get("/api/todo-groups/{id}", savedGroup.id)
                .accept(MediaType.APPLICATION_JSON)
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 반환
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("해당 TodoGroup에 대한 권한이 없습니다."))
            }
    }

    /**
     * [GET /api/todo-groups] 전체 조회 테스트.
     * - 사용자별로 필터링되어 반환되는지 검증.
     */
    @Test
    fun `getAllTodoGroups - 전체 조회`() {
        // Given: user1이 그룹 생성
        val group1 = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val group2 = TodoGroup(name = "Group2", color = "#000000", user = user1)
        todoGroupRepository.save(group1)
        todoGroupRepository.save(group2)

        // When & Then: user1 조회 시 그룹 1개가 반환됨
        mockMvc.perform(
            get("/api/todo-groups")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Int>(2)))

        // When & Then: user2 조회 시 빈 리스트가 반환됨
        mockMvc.perform(
            get("/api/todo-groups")
                .with(user(user2Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Int>(0)))
    }

    /**
     * [PUT /api/todo-groups/{id}] TodoGroup 업데이트 정상 동작 테스트.
     */
    @Test
    fun `updateTodoGroup - 정상 동작 케이스`() {
        // Given: user1이 그룹 생성
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)

        // When: 업데이트 요청
        val updateRequest = CreateTodoGroupRequest(name = "Updated Group", color = "#FFFFFF")
        val jsonRequest = asJsonString(updateRequest)
        mockMvc.perform(
            put("/api/todo-groups/{id}", savedGroup.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(user(user1Details))
        )
            // Then: 업데이트된 정보가 반환됨
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(savedGroup.id))
            .andExpect(jsonPath("$.name").value("Updated Group"))
            .andExpect(jsonPath("$.color").value("#FFFFFF"))
    }

    /**
     * [PUT /api/todo-groups/{id}] 존재하지 않는 id 업데이트 시 예외 처리 테스트.
     */
    @Test
    fun `updateTodoGroup - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 그룹 수정 요청 준비
        val nonExistentId = 999L
        val updateRequest = CreateTodoGroupRequest(name = "Updated Group", color = "#FFFFFF")
        val jsonRequest = asJsonString(updateRequest)

        // When: 존재하지 않는 id로 업데이트 요청
        mockMvc.perform(
            put("/api/todo-groups/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 반환
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId is not found"))
            }
    }

    /**
     * [PUT /api/todo-groups/{id}] 권한 없는 TodoGroup 업데이트 시 예외 처리 테스트.
     */
    @Test
    fun `updateTodoGroup - 권한 없는 경우 예외 처리`() {
        // Given: user1이 그룹 생성 후 업데이트 요청 준비
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)
        val updateRequest = CreateTodoGroupRequest(name = "Updated Group", color = "#FFFFFF")
        val jsonRequest = asJsonString(updateRequest)

        // When: user2가 업데이트 시도
        mockMvc.perform(
            put("/api/todo-groups/{id}", savedGroup.id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 반환
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("해당 TodoGroup에 대한 권한이 없습니다."))
            }
    }

    /**
     * [DELETE /api/todo-groups/{id}] TodoGroup 삭제 정상 동작 테스트.
     */
    @Test
    fun `deleteTodoGroup - 정상 동작 케이스`() {
        // Given: user1이 그룹 생성
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)
        em.flush()

        // When: 삭제 API 호출
        mockMvc.perform(
            delete("/api/todo-groups/{id}", savedGroup.id)
                .with(user(user1Details))
        )
            .andExpect(status().isNoContent)

        // Then: 삭제 후 조회 시 없어야 함
        val exists = todoGroupRepository.findById(savedGroup.id!!).isPresent
        assert(!exists)
    }

    /**
     * [DELETE /api/todo-groups/{id}] 존재하지 않는 id 삭제 시 예외 처리 테스트.
     */
    @Test
    fun `deleteTodoGroup - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 존재하지 않는 id로 삭제 요청
        val nonExistentId = 999L

        // When: 삭제 요청
        mockMvc.perform(
            delete("/api/todo-groups/{id}", nonExistentId)
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 반환
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId is not found"))
            }
    }

    /**
     * [DELETE /api/todo-groups/{id}] 권한 없는 TodoGroup 삭제 시 예외 처리 테스트.
     */
    @Test
    fun `deleteTodoGroup - 권한 없는 경우 예외 처리`() {
        // Given: user1이 그룹 생성
        val group = TodoGroup(name = "Group1", color = "#000000", user = user1)
        val savedGroup = todoGroupRepository.save(group)

        // When: user2가 삭제 시도
        mockMvc.perform(
            delete("/api/todo-groups/{id}", savedGroup.id)
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 반환
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("해당 TodoGroup에 대한 권한이 없습니다."))
            }
    }
}
