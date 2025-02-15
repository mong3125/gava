package com.example.gava.domain.todo.integration

import com.example.gava.domain.todo.dto.CreateSubTodoRequest
import com.example.gava.domain.todo.dto.CreateTodoRequest
import com.example.gava.domain.todo.entity.Icon
import com.example.gava.domain.todo.entity.TodoGroup
import com.example.gava.domain.todo.repository.SubTodoRepository
import com.example.gava.domain.todo.repository.TodoGroupRepository
import com.example.gava.domain.todo.repository.TodoIconRepository
import com.example.gava.domain.todo.repository.TodoRepository
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class TodoIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var todoRepository: TodoRepository

    @Autowired
    private lateinit var todoGroupRepository: TodoGroupRepository

    @Autowired
    private lateinit var todoIconRepository: TodoIconRepository

    @Autowired
    private lateinit var subTodoRepository: SubTodoRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var user1: User
    private lateinit var user2: User
    private lateinit var user1Details: CustomUserDetails
    private lateinit var user2Details: CustomUserDetails

    private fun asJsonString(obj: Any): String = objectMapper.writeValueAsString(obj)

    @BeforeEach
    fun setup() {
        subTodoRepository.deleteAll()
        todoRepository.deleteAll()
        todoGroupRepository.deleteAll()
        todoIconRepository.deleteAll()
        userRepository.deleteAll()

        user1 = createTestUser(userRepository, "user1")
        user2 = createTestUser(userRepository, "user2")
        user1Details = toCustomUserDetails(user1)
        user2Details = toCustomUserDetails(user2)
    }

    /**
     * [POST /api/todos] 정상적인 Todo 생성 테스트.
     */
    @Test
    fun `createTodo - 정상 동작 케이스`() {
        // Given: 아이콘 및 그룹 생성 (생성된 엔티티의 ID를 요청에 활용)
        val icon = todoIconRepository.save(
            Icon(
                name = "데이터 테스트 아이콘",
                data = "BinaryDataTest".toByteArray(),
                contentType = "image/gif"
            )
        )
        val group = todoGroupRepository.save(TodoGroup(name = "Test Group", color = "#123456", user = user1))

        // 서브 할 일 생성 요청
        val subTodoRequest = CreateSubTodoRequest(
            name = "Sub Todo 1",
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(11, 0),
            isCompleted = false
        )
        // Todo 생성 요청
        val request = CreateTodoRequest(
            name = "Test Todo",
            iconId = icon.id,
            isImportant = true,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            dueTime = LocalTime.of(10, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = listOf(group.id!!),
            color = "#FFFFFF",
            subTodos = listOf(subTodoRequest)
        )
        val jsonRequest = asJsonString(request)

        // When: Todo 생성 API 호출
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(user(user1Details))
        )
            // Then: 생성된 TodoResponse 반환 (아이콘, 그룹, 서브 할 일 포함)
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("Test Todo"))
            .andExpect(jsonPath("$.icon.id").value(icon.id))
            .andExpect(jsonPath("$.groups[0].id").value(group.id))
            .andExpect(jsonPath("$.subTodos[0].name").value("Sub Todo 1"))
    }

    /**
     * [GET /api/todos] 전체 Todo 조회 및 필터링 테스트.
     */
    @Test
    fun `getAllTodos - 전체 조회 및 필터링`() {
        // Given: 아이콘 및 그룹 생성
        val icon = todoIconRepository.save(
            Icon(
                name = "데이터 테스트 아이콘",
                data = "BinaryDataTest".toByteArray(),
                contentType = "image/gif"
            )
        )
        val group = todoGroupRepository.save(TodoGroup(name = "Test Group", color = "#123456", user = user1))
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)

        // user1이 Todo 생성 (날짜, 완료 여부, 그룹 포함)
        val requestToday = CreateTodoRequest(
            name = "Todo Today",
            iconId = icon.id,
            isImportant = false,
            isCompleted = false,
            date = today,
            startTime = LocalTime.of(8, 0),
            dueTime = LocalTime.of(9, 0),
            alarmDateTime = LocalDateTime.now().plusHours(2),
            groups = listOf(group.id!!),
            color = "#AAAAAA",
            subTodos = emptyList()
        )
        val requestTomorrow = CreateTodoRequest(
            name = "Todo Tomorrow",
            iconId = null,
            isImportant = false,
            isCompleted = true,
            date = tomorrow,
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(11, 0),
            alarmDateTime = LocalDateTime.now().plusHours(3),
            groups = emptyList(),
            color = "#BBBBBB",
            subTodos = emptyList()
        )
        // 두 건 모두 user1 소유로 생성
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestToday))
                .with(user(user1Details))
        ).andExpect(status().isOk)
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestTomorrow))
                .with(user(user1Details))
        ).andExpect(status().isOk)

        // user2가 Todo 생성 (user1의 조회 결과에 포함되지 않아야 함)
        val requestUser2 = requestToday.copy(name = "User2 Todo")
        mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestUser2))
                .with(user(user2Details))
        ).andExpect(status().isOk)

        // When & Then: user1 전체 조회 시 2건 반환
        mockMvc.perform(
            get("/api/todos")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize<Any>(2)))

        // And: 날짜 필터링 (today) 시 "Todo Today"만 반환
        mockMvc.perform(
            get("/api/todos")
                .param("date", today.toString())
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].name").value("Todo Today"))

        // And: 완료 여부 필터링 (done = true) 시 "Todo Tomorrow" 반환
        mockMvc.perform(
            get("/api/todos")
                .param("done", "true")
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$", hasSize<Any>(1)))
            .andExpect(jsonPath("$[0].name").value("Todo Tomorrow"))
    }

    /**
     * [PUT /api/todos/sub-todo/{id}] 서브 Todo 업데이트 정상 동작 테스트.
     */
    @Test
    fun `updateSubTodo - 정상 동작 케이스`() {
        // Given: 서브 Todo가 포함된 Todo 생성
        val request = CreateTodoRequest(
            name = "Todo with Sub",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            dueTime = LocalTime.of(10, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#CCCCCC",
            subTodos = listOf(
                CreateSubTodoRequest(
                    name = "Original Sub",
                    startTime = LocalTime.of(9, 30),
                    dueTime = LocalTime.of(9, 45),
                    isCompleted = false
                )
            )
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val responseJson = result.response.contentAsString
        // 생성된 TodoResponse에서 첫 번째 서브 할 일의 ID 추출
        val subTodoId = objectMapper.readTree(responseJson).get("subTodos").get(0).get("id").asLong()

        // When: 서브 Todo 업데이트 요청
        val updateRequest = CreateSubTodoRequest(
            name = "Updated Sub",
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(10, 15),
            isCompleted = true
        )
        mockMvc.perform(
            put("/api/todos/sub-todo/{id}", subTodoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest))
                .with(user(user1Details))
        )
            // Then: 업데이트된 서브 Todo 반환 (필드 값 검증)
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(subTodoId))
            .andExpect(jsonPath("$.name").value("Updated Sub"))
            .andExpect(jsonPath("$.isCompleted").value(true))
    }

    /**
     * [PUT /api/todos/sub-todo/{id}] 존재하지 않는 서브 Todo 업데이트 시 예외 처리 테스트.
     */
    @Test
    fun `updateSubTodo - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 존재하지 않는 서브 Todo id
        val nonExistentId = 9999L
        val updateRequest = CreateSubTodoRequest(
            name = "Updated Sub",
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(10, 15),
            isCompleted = true
        )
        // When: 업데이트 요청
        mockMvc.perform(
            put("/api/todos/sub-todo/{id}", nonExistentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest))
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 및 에러 메시지 포함 검증
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId"))
                assert(result.response.contentAsString.contains("조회되지 않았습니다"))
            }
    }

    /**
     * [PUT /api/todos/sub-todo/{id}] 권한 없는 서브 Todo 업데이트 시 예외 처리 테스트.
     */
    @Test
    fun `updateSubTodo - 권한 없는 경우 예외 처리`() {
        // Given: user1이 서브 Todo 포함 Todo 생성
        val request = CreateTodoRequest(
            name = "Todo with Sub",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            dueTime = LocalTime.of(10, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#CCCCCC",
            subTodos = listOf(
                CreateSubTodoRequest(
                    name = "Original Sub",
                    startTime = LocalTime.of(9, 30),
                    dueTime = LocalTime.of(9, 45),
                    isCompleted = false
                )
            )
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val responseJson = result.response.contentAsString
        val subTodoId = objectMapper.readTree(responseJson).get("subTodos").get(0).get("id").asLong()

        // When: user2가 서브 Todo 업데이트 요청
        val updateRequest = CreateSubTodoRequest(
            name = "Updated Sub",
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(10, 15),
            isCompleted = true
        )
        mockMvc.perform(
            put("/api/todos/sub-todo/{id}", subTodoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateRequest))
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 및 권한 없음 메시지 검증
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("권한이 없습니다"))
            }
    }

    /**
     * [PATCH /api/todos/{id}/done] Todo 완료 처리 정상 동작 테스트.
     */
    @Test
    fun `doneTodo - 정상 동작 케이스`() {
        // Given: isCompleted가 false인 Todo 생성
        val request = CreateTodoRequest(
            name = "Todo to be done",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(8, 0),
            dueTime = LocalTime.of(9, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#DDDDDD",
            subTodos = emptyList()
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val todoId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()

        // When: PATCH /api/todos/{id}/done 요청
        mockMvc.perform(
            patch("/api/todos/{id}/done", todoId)
                .with(user(user1Details))
        )
            // Then: 204 No Content 반환
            .andExpect(status().isNoContent)

        // And: Todo의 isCompleted가 true로 변경되었는지 검증
        val updatedTodo = todoRepository.findById(todoId).orElse(null)
        assert(updatedTodo != null && updatedTodo.isCompleted)
    }

    /**
     * [PATCH /api/todos/{id}/done] 존재하지 않는 Todo 완료 처리 시 예외 처리 테스트.
     */
    @Test
    fun `doneTodo - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 존재하지 않는 Todo id
        val nonExistentId = 9999L
        // When: PATCH 요청
        mockMvc.perform(
            patch("/api/todos/{id}/done", nonExistentId)
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 및 에러 메시지 포함 검증
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId"))
                assert(result.response.contentAsString.contains("조회되지 않았습니다"))
            }
    }

    /**
     * [PATCH /api/todos/{id}/done] 권한 없는 Todo 완료 처리 시 예외 처리 테스트.
     */
    @Test
    fun `doneTodo - 권한 없는 경우 예외 처리`() {
        // Given: user1이 Todo 생성
        val request = CreateTodoRequest(
            name = "Todo to be done",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(8, 0),
            dueTime = LocalTime.of(9, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#DDDDDD",
            subTodos = emptyList()
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val todoId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()

        // When: user2가 완료 처리 요청
        mockMvc.perform(
            patch("/api/todos/{id}/done", todoId)
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 및 권한 없음 메시지 검증
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("권한이 없습니다"))
            }
    }

    /**
     * [DELETE /api/todos/{id}] Todo 삭제 정상 동작 테스트.
     */
    @Test
    fun `deleteTodo - 정상 동작 케이스`() {
        // Given: Todo 생성
        val request = CreateTodoRequest(
            name = "Todo to delete",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(8, 0),
            dueTime = LocalTime.of(9, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#EEEEEE",
            subTodos = emptyList()
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val todoId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()
        em.flush()

        // When: DELETE /api/todos/{id} 요청
        mockMvc.perform(
            delete("/api/todos/{id}", todoId)
                .with(user(user1Details))
        )
            // Then: 204 No Content 반환
            .andExpect(status().isNoContent)

        // And: 삭제 후 해당 Todo가 존재하지 않아야 함
        val exists = todoRepository.findById(todoId).isPresent
        assert(!exists)
    }

    /**
     * [DELETE /api/todos/{id}] 존재하지 않는 Todo 삭제 시 예외 처리 테스트.
     */
    @Test
    fun `deleteTodo - 존재하지 않는 id 경우 예외 처리`() {
        // Given: 존재하지 않는 Todo id
        val nonExistentId = 9999L
        // When: DELETE 요청
        mockMvc.perform(
            delete("/api/todos/{id}", nonExistentId)
                .with(user(user1Details))
        )
            // Then: 400 Bad Request 상태 및 에러 메시지 포함 검증
            .andExpect(status().isBadRequest)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("$nonExistentId"))
                assert(result.response.contentAsString.contains("조회되지 않았습니다"))
            }
    }

    /**
     * [DELETE /api/todos/{id}] 권한 없는 Todo 삭제 시 예외 처리 테스트.
     */
    @Test
    fun `deleteTodo - 권한 없는 경우 예외 처리`() {
        // Given: user1이 Todo 생성
        val request = CreateTodoRequest(
            name = "Todo to delete",
            iconId = null,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(8, 0),
            dueTime = LocalTime.of(9, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#EEEEEE",
            subTodos = emptyList()
        )
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request))
                .with(user(user1Details))
        )
            .andExpect(status().isOk)
            .andReturn()
        val todoId = objectMapper.readTree(result.response.contentAsString).get("id").asLong()

        // When: user2가 삭제 요청
        mockMvc.perform(
            delete("/api/todos/{id}", todoId)
                .with(user(user2Details))
        )
            // Then: 403 Forbidden 상태 및 권한 없음 메시지 검증
            .andExpect(status().isForbidden)
            .andExpect { result ->
                assert(result.response.contentAsString.contains("권한이 없습니다"))
            }
    }
}
