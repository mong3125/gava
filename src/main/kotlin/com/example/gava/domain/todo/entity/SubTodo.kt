package com.example.gava.domain.todo.entity

import jakarta.persistence.*

@Entity
@Table(name = "sub_todo")
class SubTodo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id")
    val todo: Todo,                  // 부모 계획 ID (외래 키)
    var name: String,                // 세부 계획 이름
    var isCompleted: Boolean = false // 완료 여부
)
