package com.example.gava.domain.todo.controller

import com.example.gava.domain.todo.dto.CreateTodoGroupRequest
import com.example.gava.domain.todo.dto.TodoGroupResponse
import com.example.gava.domain.todo.service.TodoGroupService
import com.example.gava.security.CustomUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todo-groups")
class TodoGroupController(
    private val todoGroupService: TodoGroupService
) {
    @PostMapping
    fun create(
        @RequestBody request: CreateTodoGroupRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TodoGroupResponse> {
        val response = todoGroupService.create(request.name, request.color, userDetails.getUserId())
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getAll(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<List<TodoGroupResponse>> {
        val todoGroupResponses = todoGroupService.getAll(userDetails.getUserId())
        return ResponseEntity.ok(todoGroupResponses)
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TodoGroupResponse> {
        val todoGroupResponse = todoGroupService.getById(id, userDetails.getUserId())
        return ResponseEntity.ok(todoGroupResponse)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody request: CreateTodoGroupRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TodoGroupResponse> {
        val response = todoGroupService.update(id, request, userDetails.getUserId())
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<Unit> {
        todoGroupService.delete(id, userDetails.getUserId())
        return ResponseEntity.noContent().build()
    }
}