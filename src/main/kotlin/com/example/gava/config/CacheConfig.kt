package com.example.gava.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(): CacheManager {
        val cacheManager = CaffeineCacheManager()

        // Caffeine 설정
        cacheManager.setCaffeine(
            Caffeine.newBuilder()
                .initialCapacity(100) // 초기에 캐시에 담을 데이터 사이즈 예측치
                .maximumSize(1000)    // 최대 캐시 사이즈
                .expireAfterWrite(10, TimeUnit.MINUTES) // 쓰기 후 10분 후 만료
                .recordStats() // 캐시 통계 기록
        )

        return cacheManager
    }
}