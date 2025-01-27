package com.example.gava.domain.todo.repository

import com.example.gava.domain.todo.entity.TodoGroup
import com.example.gava.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface TodoGroupRepository : JpaRepository<TodoGroup, Long> {
    @Query("SELECT tg FROM TodoGroup tg WHERE tg.user.id = :userId")
    fun findAllByUserId(userId: Long): List<TodoGroup>
    fun findAllByUser(user: User): List<TodoGroup>

    @Query("SELECT tg FROM TodoGroup tg WHERE tg.name = :groupName AND tg.user = :currentUser")
    fun findByNameAndUser(groupName: String, currentUser: User): TodoGroup?
}
