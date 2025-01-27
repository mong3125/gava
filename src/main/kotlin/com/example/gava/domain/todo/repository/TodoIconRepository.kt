package com.example.gava.domain.todo.repository

import com.example.gava.domain.todo.entity.Icon
import org.springframework.data.jpa.repository.JpaRepository

interface TodoIconRepository: JpaRepository<Icon, Long>
