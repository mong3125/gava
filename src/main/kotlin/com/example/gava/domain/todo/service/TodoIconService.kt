package com.example.gava.domain.todo.service

import com.example.gava.domain.todo.dto.DataResponse
import com.example.gava.domain.todo.dto.TodoIconResponse
import com.example.gava.domain.todo.entity.Icon
import com.example.gava.domain.todo.repository.TodoIconRepository
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class TodoIconService (
    private val todoIconRepository: TodoIconRepository
) {
    @Transactional
    fun create(name: String, file: MultipartFile): TodoIconResponse {
        if (file.isEmpty) {
            throw CustomException(ErrorCode.FILE_IS_EMPTY, "빈 파일입니다.")
        }

        val data = file.bytes
        val contentType = file.contentType ?: "application/octet-stream"

        val todoIcon = todoIconRepository.save(
            Icon(
                name = name,
                data = data,
                contentType = contentType
            )
        )

        return TodoIconResponse(
            id = todoIcon.id!!,
            name = todoIcon.name,
            contentType = todoIcon.contentType
        )
    }

    fun getById(id: Long): TodoIconResponse {
        val todoIcon = todoIconRepository.findById(id)
            .orElseThrow{CustomException(ErrorCode.ICON_NOT_FOUND, "$id icon is not found")}

        return TodoIconResponse(
            id = todoIcon.id!!,
            name = todoIcon.name,
            contentType = todoIcon.contentType
        )
    }

    fun getAll(): List<TodoIconResponse> {
        val todoIcons = todoIconRepository.findAll()
        val todoIconResponses: List<TodoIconResponse> = todoIcons.map {
            TodoIconResponse(
                id = it.id!!,
                name = it.name,
                contentType = it.contentType
            )
        }
        return todoIconResponses
    }

    fun getIconDataById(id: Long): DataResponse {
        val todoIcon = todoIconRepository.findById(id)
            .orElseThrow{CustomException(ErrorCode.ICON_NOT_FOUND, "$id icon is not found")}

        return DataResponse(
            data = todoIcon.data,
            contentType = todoIcon.contentType
        )
    }
}
