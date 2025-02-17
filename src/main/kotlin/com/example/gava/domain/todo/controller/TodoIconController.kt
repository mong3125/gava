package com.example.gava.domain.todo.controller

import com.example.gava.domain.todo.dto.DataResponse
import com.example.gava.domain.todo.dto.TodoIconResponse
import com.example.gava.domain.todo.service.TodoIconService
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/todo-icons")
class TodoIconController (
    private val todoIconService: TodoIconService
) {
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createIcon(
        @RequestParam("name") name: String,
        @RequestParam("file") file: MultipartFile,
    ): ResponseEntity<TodoIconResponse> {
        val response = todoIconService.create(name, file)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): ResponseEntity<TodoIconResponse> {
        val todoIconResponse = todoIconService.getById(id)
        return ResponseEntity.ok(todoIconResponse)
    }

    @GetMapping
    fun getAll(): ResponseEntity<List<TodoIconResponse>> {
        val todoIconResponses = todoIconService.getAll()
        return ResponseEntity.ok(todoIconResponses)
    }

    @GetMapping("/{id}/data")
    fun getDataById(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val dataResponse: DataResponse = todoIconService.getIconDataById(id)
        val headers = HttpHeaders().apply {
            contentType = MediaType.parseMediaType(dataResponse.contentType)
            contentLength = dataResponse.data.size.toLong()
        }
        return ResponseEntity.ok().headers(headers).body(dataResponse.data)
    }
}