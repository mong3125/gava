package com.example.gava.domain.todo.dto

import com.example.gava.domain.todo.entity.Icon
import com.example.gava.domain.todo.entity.TodoGroup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TodoResponse(
    val id: Long,
    var name: String,                  // 계획 이름
    var date: LocalDate,               // 날짜
    var startTime: LocalTime? = null,         // 시작 시간
    var dueTime: LocalTime? = null,           // 종료 시간
    var color: String? = null,                // 색상
    var alarmDateTime: LocalDateTime? = null, // 알람 시각
    var isImportant: Boolean = false,   // 중요함 여부
    var isCompleted: Boolean = false, // 완료 여부
    val icon: TodoIconResponse?,
    val subTodos: List<SubTodoResponse>,
    val groups: List<TodoGroupResponse>
)

data class TodoIconResponse(
    val id: Long,
    val name: String,
    val contentType: String
) {
    companion object {
        fun fromEntity(icon: Icon): TodoIconResponse = TodoIconResponse(
            id = icon.id!!,
            name = icon.name,
            contentType = icon.contentType
        )
    }
}

data class SubTodoResponse(
    val id: Long,
    val name: String,
    val startTime: LocalTime?,
    val dueTime: LocalTime?,
    val isCompleted: Boolean
)


data class TodoGroupResponse(
    val id: Long,
    val name: String,
    val color: String?
) {
    companion object {
        fun fromEntity(todoGroup: TodoGroup): TodoGroupResponse =
            TodoGroupResponse(
                id = todoGroup.id!!,
                name = todoGroup.name,
                color = todoGroup.color
            )
    }
}