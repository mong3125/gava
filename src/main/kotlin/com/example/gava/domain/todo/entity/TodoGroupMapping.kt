package com.example.gava.domain.todo.entity

import jakarta.persistence.*

@Entity
@Table(name = "todo_group_mapping")
class TodoGroupMapping(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", referencedColumnName = "id")
    val todo: Todo? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    val group: TodoGroup? = null
)
