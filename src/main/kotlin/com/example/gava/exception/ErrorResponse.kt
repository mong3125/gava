package com.example.gava.exception

import java.time.LocalDateTime

data class ErrorResponse(
    val status: Int,
    val errorCode: String,
    val message: String,
    val timestamp: LocalDateTime
)