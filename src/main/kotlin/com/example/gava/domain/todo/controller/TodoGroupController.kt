package com.example.gava.domain.todo.controller

import com.example.gava.domain.todo.dto.CreateTodoGroupRequest
import com.example.gava.domain.todo.dto.TodoGroupResponse
import com.example.gava.domain.todo.service.TodoGroupService
import com.example.gava.domain.user.entity.User
import com.example.gava.resolver.CurrentUser
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
        @RequestBody createTodoGroupRequest: CreateTodoGroupRequest,
        @CurrentUser currentUser: User
    ): ResponseEntity<TodoGroupResponse> {
        val todoGroupResponse =
            todoGroupService.create(createTodoGroupRequest.name, createTodoGroupRequest.color, currentUser)
        return ResponseEntity.ok(todoGroupResponse)
    }

    @GetMapping("/all")
    fun getAll(@AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<List<TodoGroupResponse>> {
        val todoGroupResponses = todoGroupService.getAll(userDetails.getUserId())
        return ResponseEntity.ok(todoGroupResponses)
    }

    @GetMapping("/id/{id}")
    fun getById(@AuthenticationPrincipal userDetails: CustomUserDetails, @PathVariable id: Long): ResponseEntity<TodoGroupResponse> {
        val todoGroupResponse = todoGroupService.getById(id, userDetails.getUserId())
        return ResponseEntity.ok(todoGroupResponse)
    }

    @PutMapping("/id/{id}")
    fun update(
        @PathVariable id: Long,
        @RequestBody createTodoGroupRequest: CreateTodoGroupRequest,
        @AuthenticationPrincipal userDetails: CustomUserDetails
    ): ResponseEntity<TodoGroupResponse> {
        val todoGroupResponse = todoGroupService.update(id, createTodoGroupRequest, userDetails.getUserId())
        return ResponseEntity.ok(todoGroupResponse)
    }

    @DeleteMapping("/id/{id}")
    fun delete(@PathVariable id: Long, @AuthenticationPrincipal userDetails: CustomUserDetails): ResponseEntity<Unit> {
        todoGroupService.delete(id, userDetails.getUserId())
        return ResponseEntity.noContent().build()
    }
}