package com.example.gava.apiLog

import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class LogFlushScheduler(
    private val apiLogService: ApiLogService
) {

    /**
     * 10초(10000ms)마다 큐를 확인하여 남은 로그가 있으면 DB에 저장.
     */
    @Scheduled(fixedDelay = 10000)
    fun flushLogsPeriodically() {
        apiLogService.flushLogsToDb()
    }
}
