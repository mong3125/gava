package com.example.gava.domain.todo.controller

import com.example.gava.domain.todo.dto.CreateSubTodoRequest
import com.example.gava.domain.todo.dto.CreateTodoRequest
import com.example.gava.domain.todo.dto.SubTodoResponse
import com.example.gava.domain.todo.dto.TodoResponse
import com.example.gava.domain.todo.service.TodoService
import com.example.gava.domain.user.entity.User
import com.example.gava.resolver.CurrentUser
import com.example.gava.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/todos")
class TodoController(private val todoService: TodoService) {
    @PostMapping
    fun create(
        @RequestBody createTodoRequest: CreateTodoRequest,
        @CurrentUser currentUser: User,
    ): ResponseEntity<TodoResponse> {
        val todoResponse = todoService.create(createTodoRequest, currentUser)

        return ResponseEntity.ok(todoResponse)
    }

    @GetMapping
    fun getAll(
        @RequestParam(required = false) date: LocalDate?,
        @RequestParam(required = false) groupId: Long?,
        @RequestParam(required = false) done: Boolean?,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<List<TodoResponse>> {
        val todoResponses = todoService.getAll(userDetails.getUserId(), date, groupId, done)

        return ResponseEntity.ok(todoResponses)
    }
    
    @PutMapping("sub-todo/id/{id}")
    fun updateSubTodo(
        @PathVariable id: Long,
        @RequestBody subTodoRequest: CreateSubTodoRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<SubTodoResponse> {
        val subTodoResponse = todoService.updateSubTodo(id, subTodoRequest, userDetails.getUserId())

        return ResponseEntity.ok(subTodoResponse)
    }

    @PatchMapping("/done/id/{id}")
    fun done(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Void> {
        todoService.done(id, userDetails.getUserId())
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/id/{id}")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Unit> {
        todoService.delete(id, userDetails.getUserId())

        return ResponseEntity.noContent().build()
    }
}
