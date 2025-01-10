package com.example.gava.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.ObjectPostProcessor
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customUserDetailsService: CustomUserDetailsService
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        // CSRF 설정 비활성화
        http.csrf { it.disable() }

        // 인증이 필요한 엔드포인트와 열어둘 엔드포인트 설정
        http.authorizeHttpRequests {
            it
                .requestMatchers("/error","/api/auth/**", "/api/users/signup").permitAll() // 로그인, 회원가입 등 인증 없이 접근 가능
                .anyRequest().authenticated()   // 그 외의 요청은 인증 필요
        }

        http.exceptionHandling {
            it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        }

        // 세션 사용하지 않도록 설정 (JWT 사용 시 상태없는(Stateless) 세션 권장)
        http.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // JWT 필터를 UsernamePasswordAuthenticationFilter 이전에 삽입
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val authenticationManagerBuilder = AuthenticationManagerBuilder(object : ObjectPostProcessor<Any> {
            override fun <T : Any?> postProcess(obj: T): T {
                return obj
            }
        })

        authenticationManagerBuilder
            .userDetailsService(customUserDetailsService)
            .passwordEncoder(passwordEncoder())

        return authenticationManagerBuilder.build()
    }
}
