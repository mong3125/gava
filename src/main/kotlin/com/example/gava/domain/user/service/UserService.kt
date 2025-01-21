package com.example.gava.domain.user.service

import com.example.gava.domain.user.dto.UserInfoResponse
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import com.example.gava.security.CustomUserDetails
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional(readOnly = false)
    fun signup(username: String, password: String) {
        // 1. 사용자 중복 확인
        if (userRepository.existsByUsername(username)) {
            throw CustomException(ErrorCode.DUPLICATE_USERNAME ,"중복된 username 입니다.: $username")
        }

        // 2. 사용자 저장
        val encodedPassword = passwordEncoder.encode(password)
        val user = User(
            username = username,
            password = encodedPassword,
            roles = setOf("ROLE_USER")
        )

        userRepository.save(user)
    }

    fun getCurrentUser(): User {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as CustomUserDetails
        return userRepository.findByUsernameWithRoles(userDetails.username)
            ?: throw CustomException(ErrorCode.UNAUTHORIZED, "사용자 정보가 없습니다.")
    }

    fun getCurrentUserInfo(username: String): UserInfoResponse {
        val user = getCurrentUser()
        return UserInfoResponse(
            id = user.id,
            username = user.username,
            roles = user.roles
        )
    }
}