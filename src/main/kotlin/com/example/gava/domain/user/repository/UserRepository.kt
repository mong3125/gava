package com.example.gava.domain.user.repository

import com.example.gava.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    fun findByUsernameWithRoles(username: String): User?

    fun existsByUsername(username: String): Boolean

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :id")
    fun findByIdWithRoles(id: Long): User?
}
