package com.example.gava.apiLog

import com.example.gava.apiLog.data.ApiLog
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.util.concurrent.CompletableFuture
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Service
class ApiLogService(
    private val logRepository: ApiLogRepository
) {
    private val queue: LinkedBlockingQueue<ApiLog> = LinkedBlockingQueue()
    private val flushLock = ReentrantLock()  // 배치 저장 시 동시성 제어를 위한 락 객체
    private val BATCH_THRESHOLD = 100        // 배치 임계치

    /**
     * 로그를 큐에 적재하고, 임계치에 도달하면 즉시 DB에 저장.
     */
    @Async
    fun enqueueLog(apiLog: ApiLog): CompletableFuture<Void> {
        queue.offer(apiLog)
        if (queue.size >= BATCH_THRESHOLD) {
            flushLogsToDb() // 큐가 찼으면 즉시 배치 저장 시도
        }
        return CompletableFuture.completedFuture(null)
    }

    /**
     * 큐에 있는 로그를 배치로 DB에 저장.
     */
    fun flushLogsToDb() {
        flushLock.withLock {
            if (queue.isEmpty()) return

            val logsToInsert = mutableListOf<ApiLog>()
            queue.drainTo(logsToInsert)
            if (logsToInsert.isNotEmpty()) {
                val entities = logsToInsert.map { it.toEntity() }
                logRepository.saveAll(entities)
            }
        }
    }
}
