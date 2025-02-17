package com.example.gava.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncConfig.AsyncProperties::class)
class AsyncConfig(private val asyncProperties: AsyncProperties) : AsyncConfigurer {

    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): Executor {
        return ThreadPoolTaskExecutor().apply {
            corePoolSize = asyncProperties.corePoolSize
            maxPoolSize = asyncProperties.maxPoolSize
            queueCapacity = asyncProperties.queueCapacity
            setThreadNamePrefix(asyncProperties.threadNamePrefix)

            // 큐가 꽉 찼을 때 태스크를 실행하지 않고 단순히 버림
            setRejectedExecutionHandler(ThreadPoolExecutor.DiscardPolicy())
            // 애플리케이션 종료 시 남은 태스크를 처리하도록 설정
            setWaitForTasksToCompleteOnShutdown(true)
            initialize()
        }
    }

    @ConfigurationProperties(prefix = "async")
    class AsyncProperties {
        var corePoolSize: Int = 4
        var maxPoolSize: Int = 8
        var queueCapacity: Int = 50
        var threadNamePrefix: String = "Async-"
    }
}
