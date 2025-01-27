package com.example.gava.domain.todo.repository

import com.example.gava.domain.todo.entity.Todo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface TodoRepository : JpaRepository<Todo, Long> {

    @Query("""
        SELECT t FROM Todo t
        LEFT JOIN FETCH t._subTodos st
        LEFT JOIN FETCH t._groups tg
        LEFT JOIN FETCH t.icon i
        WHERE t.user.id = :userId
        AND (:date IS NULL OR t.date = :date)
        AND (:groupId IS NULL OR tg.id = :groupId)
        AND (:done IS NULL OR t.isCompleted = :done)
    """)
    fun findAllByUserIdAndDateAndGroupIdAndDone(
        userId: Long,
        date: LocalDate?,
        groupId: Long?,
        done: Boolean?
    ): List<Todo>
}
