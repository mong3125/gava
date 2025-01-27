package com.example.gava.domain.todo.repository

import com.example.gava.domain.todo.entity.SubTodo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubTodoRepository : JpaRepository<SubTodo, Long>