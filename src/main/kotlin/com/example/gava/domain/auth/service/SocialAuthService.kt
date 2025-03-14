package com.example.gava.domain.auth.service

import com.example.gava.domain.auth.dto.SocialUserInfo
import com.example.gava.domain.auth.dto.TokenResponse
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient

@Service
@Transactional
class SocialAuthService(
    private val userRepository: UserRepository,
    private val restClient: RestClient,
    private val authService: AuthService // 토큰 생성 로직을 포함한 Service 주입
) {

    fun loginWithSocial(provider: String, token: String): TokenResponse {
        // 1. 소셜 토큰 검증
        val socialUserInfo = verifyAndGetUserInfo(provider, token)

        // 2. 사용자 조회/생성
        val user = createOrGetUser(socialUserInfo)

        // 3. JWT 토큰 발급
        return authService.generateToken(user)
    }

    fun verifyAndGetUserInfo(provider: String, token: String): SocialUserInfo {
        return when (provider) {
            "kakao" -> verifyKakaoAccessToken(token)
            "naver" -> verifyNaverAccessToken(token)
            else -> throw CustomException(ErrorCode.INVALID_PROVIDER, "지원되지 않는 플랫폼의 소셜 로그인입니다.")
        }
    }

    private fun verifyKakaoAccessToken(accessToken: String): SocialUserInfo {
        val responseBody = restClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
            .retrieve()
            .body(Map::class.java)

        val id = responseBody?.get("id")?.toString()
        val kakaoAccount = responseBody?.get("kakao_account") as? Map<*, *>
        val email = kakaoAccount?.get("email")?.toString()

        if (id == null || email == null) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Kakao id 또는 email을 찾을 수 없습니다.")
        }

        return SocialUserInfo("kakao", id, email)
    }

    private fun verifyNaverAccessToken(accessToken: String): SocialUserInfo {
        val responseBody = restClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .body(Map::class.java)

        val response = responseBody?.get("response") as? Map<*, *>
        val id = response?.get("id")?.toString()
        val email = response?.get("email")?.toString()

        if (id == null || email == null) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Naver id 또는 email을 찾을 수 없습니다.")
        }

        return SocialUserInfo("naver", id, email)
    }

    fun createOrGetUser(socialUserInfo: SocialUserInfo): User {
        val username = "${socialUserInfo.provider}_${socialUserInfo.providerId}"
        val user = userRepository.findByUsername(username)
        if (user != null) {
            return user
        }

        // 신규 User 엔티티 생성
        val newUser = User(
            username = username,
            password = null,
            roles = setOf("ROLE_USER")
        )
        return userRepository.save(newUser)
    }
}