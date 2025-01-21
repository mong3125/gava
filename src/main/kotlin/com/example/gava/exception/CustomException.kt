package com.example.gava.exception

open class CustomException(
    val errorCode: ErrorCode,
    override val message: String
) : RuntimeException(message)