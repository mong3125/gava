package com.example.gava.domain.todo.entity

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "sub_todo")
class SubTodo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    val todo: Todo,                  // 부모 계획 ID (외래 키)

    @Column(nullable = false)
    var name: String,                // 세부 계획 이름

    var startTime: LocalTime? = null, // 시작 시간
    var dueTime: LocalTime? = null,   // 종료 시간

    var isCompleted: Boolean = false // 완료 여부
)
