package com.example.gava.apiLog.data

import java.time.LocalDateTime

data class ApiLog(
    val requestUri: String,
    val method: String,
    val requestParam: String,
    val status: Int,
    val timestamp: LocalDateTime,
    val executionTime: Long,
    val userId: Long? = null,
    val clientIp: String,
    val userAgent: String,
    val errorMessage: String? = null // 500 에러일 때 핵심 에러 메시지 저장
) {
    fun toEntity(): ApiLogEntity {
        return ApiLogEntity(
            id = null,
            requestUri = this.requestUri,
            method = this.method,
            requestParam = this.requestParam,
            status = this.status,
            timestamp = this.timestamp,
            executionTime = this.executionTime,
            userId = this.userId,
            clientIp = this.clientIp,
            userAgent = this.userAgent,
            errorMessage = this.errorMessage
        )
    }
}
