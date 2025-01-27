package com.example.gava.domain.todo.controller

import com.example.gava.domain.todo.dto.CreateTodoRequest
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
//
//    @GetMapping("/id/{id}")
//    fun getById(
//        @PathVariable id: Long,
//        @AuthenticationPrincipal userDetails: CustomUserDetails
//    ): ResponseEntity<TodoResponse> {
//        val todoResponse = todoService.getById(id, userDetails.getUserId())
//
//        return ResponseEntity.ok(todoResponse)
//    }
//
//    @PutMapping("/id/{id}")
//    fun update(
//        @PathVariable id: Long,
//        @RequestBody createTodoRequest: CreateTodoRequest,
//        @AuthenticationPrincipal userDetails: CustomUserDetails
//    ): ResponseEntity<TodoResponse> {
//        val todoResponse = todoService.update(id, createTodoRequest, userDetails.getUserId())
//
//        return ResponseEntity.ok(todoResponse)
//    }
//
//    @PatchMapping("/id/{id}/done")
//    fun done(
//        @PathVariable id: Long,
//        @AuthenticationPrincipal userDetails: CustomUserDetails
//    ): ResponseEntity<TodoResponse> {
//        val todoResponse = todoService.done(id, userDetails.getUserId())
//
//        return ResponseEntity.ok(todoResponse)
//    }
//
//    @DeleteMapping("/id/{id}")
//    fun delete(
//        @PathVariable id: Long,
//        @AuthenticationPrincipal userDetails: CustomUserDetails
//    ): ResponseEntity<Unit> {
//        todoService.delete(id, userDetails.getUserId())
//
//        return ResponseEntity.noContent().build()
//    }
}
