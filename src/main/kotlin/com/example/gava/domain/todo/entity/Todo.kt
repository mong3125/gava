package com.example.gava.domain.todo.entity

import com.example.gava.common.PrimaryKeyEntity
import com.example.gava.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Entity
@Table(name = "todo")
class Todo(
    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var date: LocalDate,

    var startTime: LocalTime? = null,
    var dueTime: LocalTime? = null,
    var color: String? = null,
    var alarmDateTime: LocalDateTime? = null,

    @Column(nullable = false)
    var isImportant: Boolean = false,
    var isCompleted: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_id")
    var icon: Icon? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User
) : PrimaryKeyEntity() {
    // === SubTodo 양방향 ===
    @OneToMany(
        mappedBy = "todo",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    private val _subTodos: MutableSet<SubTodo> = mutableSetOf()
    val subTodos: Set<SubTodo>
        get() = _subTodos

    fun addSubTodo(subTodo: SubTodo) {
        _subTodos.add(subTodo)
    }

    fun removeSubTodo(subTodo: SubTodo) {
        _subTodos.remove(subTodo)
    }

    // === TodoGroup ManyToMany ===
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "todo_group_mapping", // 중간 테이블 이름
        joinColumns = [JoinColumn(name = "todo_id")],
        inverseJoinColumns = [JoinColumn(name = "group_id")]
    )
    private val _groups: MutableSet<TodoGroup> = mutableSetOf()
    val groups: Set<TodoGroup>
        get() = _groups

    fun addGroup(todoGroup: TodoGroup) {
        _groups.add(todoGroup)
        todoGroup.todos.add(this)
    }

    fun removeGroup(todoGroup: TodoGroup) {
        _groups.remove(todoGroup)
        todoGroup.todos.remove(this)
    }
}
