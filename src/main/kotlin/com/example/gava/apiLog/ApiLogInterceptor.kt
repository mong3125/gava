package com.example.gava.apiLog

import com.example.gava.apiLog.data.ApiLog
import com.example.gava.security.CustomUserDetails
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.LocalDateTime

@Component
class ApiLogInterceptor(
    private val apiLogService: ApiLogService
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // 요청 시작 시간 기록
        request.setAttribute("startTime", System.currentTimeMillis())
        return true
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?
    ) {
        val startTime = request.getAttribute("startTime") as? Long ?: return
        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // SecurityContext에서 인증 정보 확인
        val authentication = SecurityContextHolder.getContext()?.authentication
        var userId: Long? = null
        if (authentication != null && authentication.isAuthenticated) {
            val principal = authentication.principal
            if (principal is CustomUserDetails) {
                userId = principal.getUserId()
            }
        }

        // 요청 파라미터 (Query String 기준) 및 기타 헤더 정보 추출
        val requestParam = request.queryString ?: ""
        val clientIp = getClientIp(request)
        val userAgent = request.getHeader("User-Agent") ?: ""

        // request 속성에서 errorMessage를 가져오기
        val errorMessage: String? = request.getAttribute("errorMessage") as? String

        // ApiLog 데이터 생성
        val apiLog = ApiLog(
            requestUri = request.requestURI,
            method = request.method,
            requestParam = requestParam,
            status = response.status,
            timestamp = LocalDateTime.now(),
            executionTime = executionTime,
            userId = userId,
            clientIp = clientIp,
            userAgent = userAgent,
            errorMessage = errorMessage
        )

        // 로그 큐에 적재 (배치 저장 처리)
        apiLogService.enqueueLog(apiLog)
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xfHeader = request.getHeader("X-Forwarded-For")
        return if (!xfHeader.isNullOrEmpty()) {
            xfHeader.split(",").first().trim()
        } else {
            request.remoteAddr
        }
    }
}
