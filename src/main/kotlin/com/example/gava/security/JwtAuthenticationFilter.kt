package com.example.gava.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val customUserDetailsService: CustomUserDetailsService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 1. 헤더에서 JWT 토큰 추출
        val token = resolveToken(request)

        // 2. 토큰 유효성 검사 후 SecurityContext 에 인증 정보 저장
        if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
            val username = jwtTokenProvider.getUsername(token)
            val userDetails = customUserDetailsService.loadUserByUsername(username)
            
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails, 
                null, 
                userDetails.authorities
            )

            SecurityContextHolder.getContext().authentication = authentication
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7)
        }
        return null
    }
}
