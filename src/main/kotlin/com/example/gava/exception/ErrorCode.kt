package com.example.gava.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val defaultMessage: String = "알 수 없는 오류가 발생했습니다") {
    // 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다"),
    INVALID_SOCIAL_TOKEN(HttpStatus.UNAUTHORIZED, "소셜 로그인 토큰이 유효하지 않습니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "사용자 이름 또는 비밀번호가 일치하지 않습니다"),

    // 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다"),

    // 400 Bad Request
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "사용자를 찾을 수 없습니다"),
    TODO_NOT_FOUND(HttpStatus.BAD_REQUEST, "할 일을 찾을 수 없습니다"),
    TODO_GROUP_NOT_FOUND(HttpStatus.BAD_REQUEST, "할 일 그룹을 찾을 수 없습니다"),
    INVALID_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 소셜 로그인 제공자입니다"),
    ICON_NOT_FOUND(HttpStatus.BAD_REQUEST, "아이콘을 찾을 수 없습니다"),
    SUB_TODO_NOT_FOUND(HttpStatus.BAD_REQUEST, "서브 할 일을 찾을 수 없습니다"),
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "파일이 비어있습니다"),
    DUPLICATE_USERNAME(HttpStatus.BAD_REQUEST, "이미 사용 중인 사용자 이름입니다"),
    VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),
}