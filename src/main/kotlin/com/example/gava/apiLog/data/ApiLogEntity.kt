package com.example.gava.apiLog.data

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "api_logs")
data class ApiLogEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val requestUri: String,
    val method: String,
    val requestParam: String,
    val status: Int,
    val timestamp: LocalDateTime,
    val executionTime: Long,
    val userId: Long? = null,
    val clientIp: String? = null,
    val userAgent: String? = null,
    val errorMessage: String? = null
)
