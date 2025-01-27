package com.example.gava.domain.todo.dto

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class CreateTodoRequest(
    val name: String = "",
    val iconId: Long? = null, // 아이콘 ID를 추가로 연결
    val isImportant: Boolean = false,
    val isCompleted: Boolean = false,
    val date: LocalDate = LocalDate.now(), // ISO-8601 형식 (예: "2025-01-05")
    val startTime: LocalTime? = null, // ISO-8601 형식 (예: "10:00:00")
    val dueTime: LocalTime? = null, // ISO-8601 형식 (예: "12:00:00")
    val alarmDateTime: LocalDateTime?, // 알람 시각
    val groups: List<Long> = emptyList(),
    val color: String? = null,
    val subTodos: List<CreateSubTodoRequest> = emptyList()
)

data class CreateSubTodoRequest(
    val name: String,
    val startTime: LocalTime?,
    val dueTime: LocalTime?,
    val isCompleted: Boolean = false
)

data class CreateTodoGroupRequest(
    val name: String,
    val color: String
)
