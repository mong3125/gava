// src/test/kotlin/com/example/gava/integration/TodoIntegrationTest.kt
package com.example.gava.domain.todo.integration

import com.example.gava.domain.todo.dto.CreateSubTodoRequest
import com.example.gava.domain.todo.dto.CreateTodoRequest
import com.example.gava.domain.todo.entity.Todo
import com.example.gava.domain.todo.repository.SubTodoRepository
import com.example.gava.domain.todo.repository.TodoRepository
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.CustomUserDetails
import com.example.gava.util.TestSecurityUtils.createTestUser
import com.example.gava.util.TestSecurityUtils.toCustomUserDetails
import com.example.gava.util.TestSecurityUtils.withUser
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var todoRepository: TodoRepository

    @Autowired
    lateinit var subTodoRepository: SubTodoRepository

    @Autowired
    lateinit var userRepository: UserRepository

    // 테스트에 사용할 두 사용자: 정상 요청용, 권한이 없는 사용자(비정상 요청)
    lateinit var testUser: User
    lateinit var unauthorizedUser: User

    lateinit var testUserDetails: CustomUserDetails
    lateinit var unauthorizedUserDetails: CustomUserDetails

    @BeforeEach
    fun setup() {
        // DB 초기화 (각 테스트마다 독립적인 상태 유지)
        todoRepository.deleteAll()
        userRepository.deleteAll()

        testUser = createTestUser(userRepository, "testUser")
        unauthorizedUser = createTestUser(userRepository, "unauthorizedUser")
        testUserDetails = toCustomUserDetails(testUser)
        unauthorizedUserDetails = toCustomUserDetails(unauthorizedUser)
    }

    @Test
    fun `createTodo - 성공`() {
        // Given: 유효한 CreateTodoRequest (아이콘, 그룹 없이) 및 testUser 인증 정보
        val request = CreateTodoRequest(
            name = "Integration Test Todo",
            iconId = null,
            isImportant = true,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(12, 0),
            alarmDateTime = LocalDateTime.now().plusHours(1),
            groups = emptyList(),
            color = "#FFFFFF",
            subTodos = listOf(
                CreateSubTodoRequest(
                    name = "SubTodo1",
                    startTime = LocalTime.of(10, 15),
                    dueTime = LocalTime.of(10, 45),
                    isCompleted = false
                )
            )
        )
        val jsonRequest = objectMapper.writeValueAsString(request)

        // When: POST /api/todos 요청 시 testUser의 인증 정보 주입
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andReturn()

        // Then: 응답 결과 및 DB에 Todo가 생성되었음을 검증
        val responseBody = result.response.contentAsString
        @Suppress("UNCHECKED_CAST")
        val responseMap = objectMapper.readValue(responseBody, Map::class.java) as Map<String, Any>
        assertEquals("Integration Test Todo", responseMap["name"])

        val todos = todoRepository.findAll()
        assertEquals(1, todos.size)
        assertEquals("Integration Test Todo", todos[0].name)
    }

    @Test
    fun `createTodo - 실패_아이콘_없음`() {
        // Given: 존재하지 않는 아이콘 ID(예: 9999)를 가진 요청 및 testUser 인증 정보
        val request = CreateTodoRequest(
            name = "Invalid Icon Todo",
            iconId = 9999,
            isImportant = false,
            isCompleted = false,
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            alarmDateTime = null,
            groups = emptyList(),
            color = null,
            subTodos = emptyList()
        )
        val jsonRequest = objectMapper.writeValueAsString(request)

        // When: POST /api/todos 요청
        val result = mockMvc.perform(
            post("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(withUser(testUserDetails))
        )
            // Then: 아이콘 미존재에 따른 에러 응답 (예: 400 Bad Request)
            .andExpect(status().isBadRequest)
            .andReturn()

        val responseBody = result.response.contentAsString
        assertTrue(responseBody.contains("다음 아이콘 ID가 조회되지 않았습니다"))
    }

    @Test
    fun `getAllTodos - 성공_필터없음`() {
        // Given: testUser의 Todo 2개와 unauthorizedUser의 Todo 1개를 DB에 삽입
        val todo1 = Todo(
            name = "Todo1",
            date = LocalDate.now(),
            startTime = LocalTime.of(9, 0),
            dueTime = LocalTime.of(10, 0),
            color = "#FF0000",
            alarmDateTime = LocalDateTime.now().plusMinutes(30),
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = testUser
        )
        val todo2 = Todo(
            name = "Todo2",
            date = LocalDate.now(),
            startTime = LocalTime.of(11, 0),
            dueTime = LocalTime.of(12, 0),
            color = "#00FF00",
            alarmDateTime = LocalDateTime.now().plusMinutes(45),
            isImportant = true,
            isCompleted = true,
            icon = null,
            user = testUser
        )
        val todoOther = Todo(
            name = "Other User Todo",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = unauthorizedUser
        )
        todoRepository.saveAll(listOf(todo1, todo2, todoOther))

        // When: GET /api/todos 요청 시 testUser 인증 정보 주입
        val result = mockMvc.perform(
            get("/api/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isOk)
            .andReturn()

        // Then: 응답에는 testUser의 Todo 2건만 포함됨
        val responseBody = result.response.contentAsString
        @Suppress("UNCHECKED_CAST")
        val todosList = objectMapper.readValue(responseBody, List::class.java) as List<Map<String, Any>>
        assertEquals(2, todosList.size)
        val names = todosList.map { it["name"] }
        assertTrue(names.contains("Todo1"))
        assertTrue(names.contains("Todo2"))
    }

    @Test
    fun `updateSubTodo - 성공`() {
        // Given: testUser의 Todo와 연결된 SubTodo 생성
        val todo = Todo(
            name = "Todo with SubTodo",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = testUser
        )
        val savedTodo = todoRepository.save(todo)
        val subTodo = com.example.gava.domain.todo.entity.SubTodo(
            name = "Original SubTodo",
            todo = savedTodo,
            startTime = LocalTime.of(10, 0),
            dueTime = LocalTime.of(10, 30),
            isCompleted = false
        )
        val savedSubTodo = subTodoRepository.save(subTodo)

        // When: PUT /api/todos/sub-todo/id/{id} 요청으로 SubTodo 업데이트
        val updateRequest = CreateSubTodoRequest(
            name = "Updated SubTodo",
            startTime = LocalTime.of(10, 15),
            dueTime = LocalTime.of(10, 45),
            isCompleted = true
        )
        val jsonRequest = objectMapper.writeValueAsString(updateRequest)
        val result = mockMvc.perform(
            put("/api/todos/sub-todo/id/${savedSubTodo.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isOk)
            .andReturn()

        // Then: 응답 및 DB에서 SubTodo 업데이트 확인
        val responseBody = result.response.contentAsString
        @Suppress("UNCHECKED_CAST")
        val responseMap = objectMapper.readValue(responseBody, Map::class.java) as Map<String, Any>
        assertEquals("Updated SubTodo", responseMap["name"])

        val updatedSubTodo = subTodoRepository.findById(savedSubTodo.id!!).orElseThrow()
        assertEquals("Updated SubTodo", updatedSubTodo.name)
        assertTrue(updatedSubTodo.isCompleted)
    }

    @Test
    fun `updateSubTodo - 실패_서브할일_없음`() {
        // Given: 존재하지 않는 서브할일 ID(예: 9999)를 대상으로 요청
        val updateRequest = CreateSubTodoRequest(
            name = "Should Fail",
            startTime = LocalTime.of(9, 0),
            dueTime = LocalTime.of(9, 30),
            isCompleted = false
        )
        val jsonRequest = objectMapper.writeValueAsString(updateRequest)

        // When: PUT 요청 시 testUser 인증 정보 주입
        val result = mockMvc.perform(
            put("/api/todos/sub-todo/id/9999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest)
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isBadRequest)
            .andReturn()

        // Then: 응답 메시지에 '서브 할 일 ID가 조회되지 않았습니다' 포함 여부 확인
        val responseBody = result.response.contentAsString
        assertTrue(responseBody.contains("다음 서브 할 일 ID가 조회되지 않았습니다"))
    }

    @Test
    fun `doneTodo - 성공`() {
        // Given: testUser의 Todo 생성 (isCompleted=false)
        val todo = Todo(
            name = "Todo to be Done",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = testUser
        )
        val savedTodo = todoRepository.save(todo)

        // When: PATCH /api/todos/done/id/{id} 요청 시 testUser 인증 정보 주입
        mockMvc.perform(
            patch("/api/todos/done/id/${savedTodo.id}")
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isNoContent)

        // Then: DB에서 Todo의 isCompleted 값이 true로 변경되었음을 확인
        val updatedTodo = todoRepository.findById(savedTodo.id!!).orElseThrow()
        assertTrue(updatedTodo.isCompleted)
    }

    @Test
    fun `doneTodo - 실패_권한없음`() {
        // Given: unauthorizedUser 소유의 Todo 생성
        val todo = Todo(
            name = "Todo Unauthorized",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = unauthorizedUser
        )
        val savedTodo = todoRepository.save(todo)

        // When: testUser 인증 정보로 PATCH 요청 → 권한 부족으로 실패
        val result = mockMvc.perform(
            patch("/api/todos/done/id/${savedTodo.id}")
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isForbidden)
            .andReturn()

        // Then: 응답 메시지에 '수정 권한이 없습니다' 포함 여부 확인
        val responseBody = result.response.contentAsString
        assertTrue(responseBody.contains("수정 권한이 없습니다"))
    }

    @Test
    fun `deleteTodo - 성공`() {
        // Given: testUser의 Todo 생성
        val todo = Todo(
            name = "Todo to Delete",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = testUser
        )
        val savedTodo = todoRepository.save(todo)

        // When: DELETE /api/todos/id/{id} 요청 시 testUser 인증 정보 주입
        mockMvc.perform(
            delete("/api/todos/id/${savedTodo.id}")
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isNoContent)

        // Then: DB에서 해당 Todo가 삭제되었음을 확인
        val exists = todoRepository.existsById(savedTodo.id!!)
        assertFalse(exists)
    }

    @Test
    fun `deleteTodo - 실패_권한없음`() {
        // Given: unauthorizedUser 소유의 Todo 생성
        val todo = Todo(
            name = "Todo Unauthorized Delete",
            date = LocalDate.now(),
            startTime = null,
            dueTime = null,
            color = null,
            alarmDateTime = null,
            isImportant = false,
            isCompleted = false,
            icon = null,
            user = unauthorizedUser
        )
        val savedTodo = todoRepository.save(todo)

        // When: testUser 인증 정보로 DELETE 요청 → 권한 부족으로 실패
        val result = mockMvc.perform(
            delete("/api/todos/id/${savedTodo.id}")
                .with(withUser(testUserDetails))
        )
            .andExpect(status().isForbidden)
            .andReturn()

        // Then: 응답 메시지에 '삭제 권한이 없습니다' 포함 여부 확인
        val responseBody = result.response.contentAsString
        assertTrue(responseBody.contains("삭제 권한이 없습니다"))
    }
}
