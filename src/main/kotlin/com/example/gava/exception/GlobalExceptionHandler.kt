package com.example.gava.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {
    private fun buildResponse(
        status: HttpStatus,
        code: ErrorCode,
        message: String? = null
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(status).body(
            ErrorResponse(
                status = status.value(),
                errorCode = code.name,
                message = message ?: code.defaultMessage,
                timestamp = LocalDateTime.now()
            )
        )
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(ex: CustomException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return buildResponse(ex.errorCode.status, ex.errorCode, ex.message)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = ex.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, message)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleInvalidFormat(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED, ex.message)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAccessDenied(ex: AuthorizationDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, ex.message)
    }
}
