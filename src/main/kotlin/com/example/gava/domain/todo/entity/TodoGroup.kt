package com.example.gava.domain.todo.entity

import com.example.gava.domain.user.entity.User
import jakarta.persistence.*

@Entity
@Table(
    name = "todo_group",
    indexes = [Index(name = "idx_todo_group_name", columnList = "name")]
)

data class TodoGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,   // 그룹 이름
    val color: String?, // 그룹 색상 (Hex 코드)

    @ManyToOne(fetch = FetchType.LAZY)
    val user: User    // 사용자
) {
    constructor() : this(null, "", null, User())
}
