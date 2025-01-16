package com.example.gava.domain.todo.entity

import com.example.gava.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "todo")
data class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,                  // 계획 이름
    val date: LocalDate,               // 날짜
    val startTime: LocalTime?,         // 시작 시간
    val dueTime: LocalTime?,           // 종료 시간
    val color: String?,                // 색상
    val alarmDateTime: LocalDateTime?, // 알람 시각
    val isImportant: Boolean = false,   // 중요함 여부
    val isCompleted: Boolean = false, // 완료 여부

    @OneToOne(fetch = FetchType.LAZY)
    val icon: Icon,                 // 아이콘

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User                   // 사용자
)

