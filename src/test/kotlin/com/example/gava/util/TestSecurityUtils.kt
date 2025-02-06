package com.example.gava.util

import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.security.CustomUserDetails
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.web.servlet.request.RequestPostProcessor

object TestSecurityUtils {

    /**
     * 일반 사용자 생성 메서드
     * @param userRepository 테스트용 UserRepository
     * @param username 사용자 이름
     * @return 저장된 User 엔티티
     */
    fun createTestUser(userRepository: UserRepository, username: String): User {
        val user = User(
            id = null,
            username = username,
            password = "password",
            roles = setOf("ROLE_USER")
        )
        return userRepository.save(user)
    }

    /**
     * 관리자 사용자 생성 메서드
     * @param userRepository 테스트용 UserRepository
     * @param username 관리자 이름
     * @return 저장된 User 엔티티
     */
    fun createTestAdmin(userRepository: UserRepository, username: String): User {
        val user = User(
            id = null,
            username = username,
            password = "password",
            roles = setOf("ROLE_ADMIN")
        )
        return userRepository.save(user)
    }

    /**
     * User 엔티티를 CustomUserDetails로 변환하는 메서드
     */
    fun toCustomUserDetails(user: User): CustomUserDetails {
        return CustomUserDetails(
            userId = user.id!!,
            username = user.username,
            password = user.password!!,
            roles = user.roles.map { SimpleGrantedAuthority(it) }
        )
    }

    /**
     * Spring Security 테스트용 RequestPostProcessor를 생성하는 메서드.
     * 이를 통해 MockMvc 요청 시 .with(user(userDetails)) 형태로 사용할 수 있습니다.
     */
    fun withUser(userDetails: CustomUserDetails): RequestPostProcessor {
        return SecurityMockMvcRequestPostProcessors.user(userDetails)
    }
}
