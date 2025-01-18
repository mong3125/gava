package com.example.gava.config

import com.example.gava.resolver.UserArgumentResolver
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val userArgumentResolver: UserArgumentResolver
) : WebMvcConfigurer {

    // WebMvcConfigurer 인터페이스를 구현하여 커스텀한 HandlerMethodArgumentResolver를 추가
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(userArgumentResolver)
    }

    @Bean
    fun webClient(): WebClient {
        return WebClient.builder().build()
    }
}
