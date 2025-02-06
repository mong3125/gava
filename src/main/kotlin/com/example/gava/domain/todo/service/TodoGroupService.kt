package com.example.gava.domain.todo.service

import com.example.gava.domain.todo.dto.CreateTodoGroupRequest
import com.example.gava.domain.todo.dto.TodoGroupResponse
import com.example.gava.domain.todo.entity.TodoGroup
import com.example.gava.domain.todo.repository.TodoGroupRepository
import com.example.gava.domain.user.entity.User
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TodoGroupService(
    private val todoGroupRepository: TodoGroupRepository,
) {
    @Transactional
    fun create(name: String, color: String, user: User): TodoGroupResponse {
        val todoGroup = TodoGroup(name = name, color = color, user = user)
        val savedGroup = todoGroupRepository.save(todoGroup)
        return TodoGroupResponse.fromEntity(savedGroup)
    }

    fun getAll(userId: Long): List<TodoGroupResponse> =
        todoGroupRepository.findAllByUserId(userId)
            .map { TodoGroupResponse.fromEntity(it) }


    fun getById(id: Long, userId: Long): TodoGroupResponse? {
        val todoGroup = todoGroupRepository.findById(id).orElseThrow{CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found")}
        checkUserAuthorization(todoGroup, userId)
        return TodoGroupResponse.fromEntity(todoGroup)
    }

    @Transactional
    fun update(id: Long, request: CreateTodoGroupRequest, userId: Long): TodoGroupResponse? {
        val todoGroup = todoGroupRepository.findById(id)
            .orElseThrow{ CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found") }
        checkUserAuthorization(todoGroup, userId)
        todoGroup.apply {
            name = request.name
            color = request.color
        }
        return TodoGroupResponse.fromEntity(todoGroup)
    }

    @Transactional(readOnly = false)
    fun delete(id: Long, userId: Long) {
        val todoGroup = todoGroupRepository.findById(id)
            .orElseThrow{ CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found") }
        checkUserAuthorization(todoGroup, userId)
        todoGroupRepository.delete(todoGroup)
    }

    private fun checkUserAuthorization(todoGroup: TodoGroup, userId: Long) {
        if (todoGroup.user?.id != userId) {
            throw CustomException(ErrorCode.FORBIDDEN, "해당 TodoGroup에 대한 권한이 없습니다.")
        }
    }
}
