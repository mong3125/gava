package com.example.gava.domain.todo.entity

import com.example.gava.domain.user.entity.User
import jakarta.persistence.*

@Entity
@Table(
    name = "todo_group",
    indexes = [Index(name = "idx_todo_group_name", columnList = "name")]
)
class TodoGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,   // 그룹 이름

    @Column(nullable = false)
    var color: String, // 그룹 색상 (Hex 코드)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,    // 사용자

    @ManyToMany(mappedBy = "_groups", fetch = FetchType.LAZY)
    val todos: MutableSet<Todo> = mutableSetOf() // 계획
)
