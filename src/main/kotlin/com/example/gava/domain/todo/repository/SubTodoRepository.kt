package com.example.gava.domain.todo.repository

import com.example.gava.domain.todo.entity.SubTodo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SubTodoRepository : JpaRepository<SubTodo, Long> {
    @Query("SELECT st FROM SubTodo st LEFT JOIN FETCH st.todo WHERE st.id = :id")
    fun findSubTodoById(id: Long): SubTodo?
}