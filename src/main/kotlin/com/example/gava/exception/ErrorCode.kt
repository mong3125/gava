package com.example.gava.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus) {
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN),

    // 400 Bad Request
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST),
    TODO_NOT_FOUND(HttpStatus.BAD_REQUEST),
    TODO_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST),
    ICON_NOT_FOUND(HttpStatus.BAD_REQUEST),
    SUB_TODO_NOT_FOUND(HttpStatus.BAD_REQUEST),
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST),
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
}