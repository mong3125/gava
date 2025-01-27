package com.example.gava.domain.todo.service

import com.example.gava.domain.todo.dto.*
import com.example.gava.domain.todo.entity.Icon
import com.example.gava.domain.todo.entity.SubTodo
import com.example.gava.domain.todo.entity.Todo
import com.example.gava.domain.todo.entity.TodoGroup
import com.example.gava.domain.todo.repository.TodoGroupRepository
import com.example.gava.domain.todo.repository.TodoIconRepository
import com.example.gava.domain.todo.repository.TodoRepository
import com.example.gava.domain.user.entity.User
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class TodoService(
    private val todoRepository: TodoRepository,
    private val todoGroupRepository: TodoGroupRepository,
    private val todoIconRepository: TodoIconRepository
) {

    @Transactional
    fun create(request: CreateTodoRequest, user: User): TodoResponse {
        val icon = request.iconId?.let { findIconById(it) }
        val groups = findTodoGroupsByIds(request.groups)
        val todo = createTodoEntity(request, icon, user)
        val subTodos = createSubTodoEntities(request.subTodos, todo)

        val savedTodo = saveTodoAll(todo, groups, subTodos)

        return toTodoResponse(savedTodo, icon, groups, subTodos)
    }

    private fun findIconById(iconId: Long): Icon {
        return todoIconRepository.findById(iconId).orElseThrow {
            CustomException(
                ErrorCode.ICON_NOT_FOUND,
                "다음 아이콘 ID가 조회되지 않았습니다: $iconId"
            )
        }
    }

    private fun findTodoGroupsByIds(groupIds: List<Long>): List<TodoGroup> {
        if (groupIds.isEmpty()) return emptyList()
        val todoGroups = todoGroupRepository.findAllById(groupIds)
        if (todoGroups.size != groupIds.size) {
            val foundIds = todoGroups.mapNotNull { it.id }
            val notFoundIds = groupIds.filterNot { foundIds.contains(it) }
            if (notFoundIds.isNotEmpty()) {
                throw CustomException(
                    ErrorCode.TODO_GROUP_NOT_FOUND,
                    "다음 그룹 ID가 조회되지 않았습니다: $notFoundIds"
                )
            }
        }
        return todoGroups
    }

    private fun createTodoEntity(
        request: CreateTodoRequest,
        icon: Icon?,
        currentUser: User
    ): Todo {
        return Todo(
            name = request.name,
            date = request.date,
            startTime = request.startTime,
            dueTime = request.dueTime,
            color = request.color,
            alarmDateTime = request.alarmDateTime,
            isImportant = request.isImportant,
            isCompleted = request.isCompleted,
            icon = icon,
            user = currentUser
        )
    }

    private fun createSubTodoEntities(
        subTodoRequests: List<CreateSubTodoRequest>,
        todo: Todo
    ): List<SubTodo> {
        return subTodoRequests.map {
            SubTodo(
                name = it.name,
                todo = todo,
                startTime = it.startTime,
                dueTime = it.dueTime,
                isCompleted = it.isCompleted
            )
        }
    }

    private fun saveTodoAll(
        todo: Todo,
        todoGroups: List<TodoGroup>,
        subTodos: List<SubTodo>
    ): Todo {
        subTodos.forEach(todo::addSubTodo)
        todoGroups.forEach(todo::addGroup)

        return todoRepository.save(todo)
    }

    private fun toTodoResponse(
        todo: Todo,
        icon: Icon?,
        groups: List<TodoGroup>,
        subTodos: List<SubTodo>
    ): TodoResponse {

        val iconResponse = icon?.let {
            TodoIconResponse(
                id = it.id!!,
                name = it.name,
                contentType = it.contentType
            )
        }

        val groupResponses = groups.map {
            TodoGroupResponse(
                id = it.id!!,
                name = it.name,
                color = it.color
            )
        }

        val subTodoResponses = subTodos.map {
            SubTodoResponse(
                id = it.id!!,
                name = it.name,
                startTime = it.startTime,
                dueTime = it.dueTime,
                isCompleted = it.isCompleted
            )
        }

        return TodoResponse(
            id = todo.id!!,
            name = todo.name,
            date = todo.date,
            startTime = todo.startTime,
            dueTime = todo.dueTime,
            color = todo.color,
            alarmDateTime = todo.alarmDateTime,
            isImportant = todo.isImportant,
            isCompleted = todo.isCompleted,
            icon = iconResponse,
            groups = groupResponses,
            subTodos = subTodoResponses
        )
    }

    private fun toTodoResponse(
        todo: Todo
    ): TodoResponse {
        val iconResponse = todo.icon?.let {
            TodoIconResponse(
                id = it.id!!,
                name = it.name,
                contentType = it.contentType
            )
        }

        val groupResponses = todo.groups.map {
            TodoGroupResponse(
                id = it.id!!,
                name = it.name,
                color = it.color
            )
        }

        val subTodoResponses = todo.subTodos.map {
            SubTodoResponse(
                id = it.id!!,
                name = it.name,
                startTime = it.startTime,
                dueTime = it.dueTime,
                isCompleted = it.isCompleted
            )
        }

        return TodoResponse(
            id = todo.id!!,
            name = todo.name,
            date = todo.date,
            startTime = todo.startTime,
            dueTime = todo.dueTime,
            color = todo.color,
            alarmDateTime = todo.alarmDateTime,
            isImportant = todo.isImportant,
            isCompleted = todo.isCompleted,
            icon = iconResponse,
            groups = groupResponses,
            subTodos = subTodoResponses
        )
    }

    fun getAll(userId: Long, date: LocalDate?, groupId: Long?, done: Boolean?): List<TodoResponse> {
        val todos = todoRepository.findAllByUserIdAndDateAndGroupIdAndDone(userId, date, groupId, done)

        return todos.map { toTodoResponse(it) }
    }

}
