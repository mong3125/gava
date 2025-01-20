package com.example.gava.domain.todo.entity

import com.example.gava.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "todo")
class Todo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    var name: String,                  // 계획 이름
    var date: LocalDate,               // 날짜
    var startTime: LocalTime? = null,         // 시작 시간
    var dueTime: LocalTime? = null,           // 종료 시간
    var color: String? = null,                // 색상
    var alarmDateTime: LocalDateTime? = null, // 알람 시각
    var isImportant: Boolean = false,   // 중요함 여부
    var isCompleted: Boolean = false, // 완료 여부

    @OneToOne(fetch = FetchType.LAZY)
    var icon: Icon,                 // 아이콘

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User                   // 사용자
)

