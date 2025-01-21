package com.example.gava.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = ex.errorCode.status.value(),
            errorCode = ex.errorCode.name,
            message = ex.message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(errorResponse, ex.errorCode.status)
    }
}
