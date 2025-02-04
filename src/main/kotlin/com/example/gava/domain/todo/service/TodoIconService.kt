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
            throw CustomException(ErrorCode.FILE_IS_EMPTY, "업로드된 파일이 비어있습니다.")
        }

        val icon = Icon(
            name = name,
            data = file.bytes,
            contentType = file.contentType ?: "application/octet-stream"
        )
        val savedIcon = todoIconRepository.save(icon)

        return TodoIconResponse.fromEntity(savedIcon)
    }

    fun getById(id: Long): TodoIconResponse {
        val icon = todoIconRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.ICON_NOT_FOUND, "Icon id: $id 를 찾을 수 없습니다.") }
        return TodoIconResponse.fromEntity(icon)
    }

    fun getAll(): List<TodoIconResponse> {
        return todoIconRepository.findAll().map { TodoIconResponse.fromEntity(it) }
    }

    fun getIconDataById(id: Long): DataResponse {
        val icon = todoIconRepository.findById(id)
            .orElseThrow { CustomException(ErrorCode.ICON_NOT_FOUND, "Icon id: $id 를 찾을 수 없습니다.") }
        return DataResponse(
            data = icon.data,
            contentType = icon.contentType
        )
    }
}
