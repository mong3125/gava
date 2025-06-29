package com.example.gava.config

import com.example.gava.apiLog.ApiLogInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val apiLogInterceptor: ApiLogInterceptor
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiLogInterceptor)
            .addPathPatterns("/api/**") // 모든 경로에 적용
    }

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder()
            .defaultHeaders { headers ->
                headers.set("Accept", "application/json")
            }
            .build()
    }
}
