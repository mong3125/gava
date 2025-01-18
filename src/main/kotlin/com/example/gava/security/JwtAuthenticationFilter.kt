package com.example.gava.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 헤더에서 JWT 토큰 추출
        val token = resolveToken(request)

        // 토큰 유효성 검사 후 SecurityContext 에 인증 정보 저장
        if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
            val userId: Long = jwtTokenProvider.getUserId(token)
            val username = jwtTokenProvider.getUsername(token)
            val roles = jwtTokenProvider.getRoles(token)
            val authorities = roles.map { role -> SimpleGrantedAuthority(role) }

            val userDetails = CustomUserDetails(userId, username, "", authorities)
            val authentication = UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                authorities
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
