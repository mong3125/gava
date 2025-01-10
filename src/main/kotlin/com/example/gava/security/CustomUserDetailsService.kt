package com.example.gava.security

import com.example.gava.domain.user.repository.UserRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    @Override
    @Cacheable(value = ["userDetailsCache"], key = "#username", unless = "#result == null")
    override fun loadUserByUsername(username: String): UserDetails {
        val userEntity = userRepository.findByUsernameWithRoles(username)
            ?: throw UsernameNotFoundException("사용자를 찾을 수 없습니다: $username")

        // 엔티티에서 roles를 꺼내서 SimpleGrantedAuthority 형태로 변환
        val authorities = userEntity.roles.map { role -> SimpleGrantedAuthority(role) }

        // 예시: userEntity에 password, roles 등을 담아두었다 가정
        return CustomUserDetails(
            userEntity.id!!,
            userEntity.username,
            userEntity.password,
            authorities
        )
    }
}
