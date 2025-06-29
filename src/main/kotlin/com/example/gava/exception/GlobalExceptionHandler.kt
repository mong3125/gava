package com.example.gava.exception

import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime
import java.util.concurrent.TimeoutException

@ControllerAdvice
class GlobalExceptionHandler: ResponseEntityExceptionHandler() {
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

    @ExceptionHandler(TimeoutException::class)
    fun handleTimeoutException(ex: TimeoutException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.REQUEST_TIMEOUT, ErrorCode.REQUEST_TIMEOUT, "요청 처리 시간이 초과되었습니다.")
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: org.springframework.http.HttpHeaders,
        status: org.springframework.http.HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val message = ex.bindingResult.fieldErrors.joinToString(", ") {
            "${it.field}: ${it.defaultMessage}"
        }

        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = ErrorCode.VALIDATION_FAILED.name,
            message = message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: org.springframework.http.HttpHeaders,
        status: org.springframework.http.HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            errorCode = ErrorCode.VALIDATION_FAILED.name,
            message = ex.message ?: ErrorCode.VALIDATION_FAILED.defaultMessage,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(AuthorizationDeniedException::class)
    fun handleAccessDenied(ex: AuthorizationDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return buildResponse(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED, ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        request.setAttribute("errorMessage", "${ex::class.simpleName}: ${ex.message ?: "No message"}")
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, ex.message)
    }
}
