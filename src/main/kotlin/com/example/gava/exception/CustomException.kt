package com.example.gava.exception

import org.springframework.http.HttpStatus

open class CustomException(
    val status: HttpStatus,
    val errorCode: String,
    override val message: String
) : RuntimeException(message)