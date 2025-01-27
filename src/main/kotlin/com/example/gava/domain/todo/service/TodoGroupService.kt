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
    @Transactional(readOnly = false)
    fun create(name: String, color: String, user: User): TodoGroupResponse {
        val todoGroup: TodoGroup = todoGroupRepository.save(TodoGroup(name = name, color = color, user = user))

        return TodoGroupResponse(
            id = todoGroup.id!!,
            name = todoGroup.name,
            color = todoGroup.color
        )
    }

    fun getAll(userId: Long): List<TodoGroupResponse> {
        val todoGroups: List<TodoGroup> = todoGroupRepository.findAllByUserId(userId)
        val todoGroupResponses: List<TodoGroupResponse> = todoGroups.map {
            TodoGroupResponse(
                id = it.id!!,
                name = it.name,
                color = it.color
            )
        }
        return todoGroupResponses
    }

    fun getById(id: Long, userId: Long): TodoGroupResponse? {
        val todoGroup = todoGroupRepository.findById(id).orElseThrow{CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found")}

        if (todoGroup.user?.id != userId) {
            throw CustomException(ErrorCode.FORBIDDEN, "todo group 수정 권한이 없습니다.")
        }

        return TodoGroupResponse(
            id = todoGroup.id!!,
            name = todoGroup.name,
            color = todoGroup.color
        )
    }

    @Transactional(readOnly = false)
    fun update(id: Long, createTodoGroupRequest: CreateTodoGroupRequest, userId: Long): TodoGroupResponse? {
        val todoGroup = todoGroupRepository.findById(id).orElseThrow{CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found")}

        if (todoGroup.user?.id != userId) {
            throw CustomException(ErrorCode.FORBIDDEN, "todo group 수정 권한이 없습니다.")
        }

        todoGroup.name = createTodoGroupRequest.name
        todoGroup.color = createTodoGroupRequest.color

        return TodoGroupResponse(
            id = todoGroup.id!!,
            name = todoGroup.name,
            color = todoGroup.color
        )
    }

    @Transactional(readOnly = false)
    fun delete(id: Long, userId: Long) {
        val todoGroup = todoGroupRepository.findById(id).orElseThrow{CustomException(ErrorCode.TODO_GROUP_NOT_FOUND, "$id is not found")}

        if (todoGroup.user?.id != userId) {
            throw CustomException(ErrorCode.FORBIDDEN, "todo group 삭제 권한이 없습니다.")
        }

        todoGroupRepository.delete(todoGroup)
    }
}
