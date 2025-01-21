package com.example.gava.domain.auth.service

import com.example.gava.domain.auth.dto.SocialUserInfo
import com.example.gava.domain.user.entity.User
import com.example.gava.domain.user.repository.UserRepository
import com.example.gava.exception.CustomException
import com.example.gava.exception.ErrorCode
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient

@Service
@Transactional
class SocialAuthService(
    private val userRepository: UserRepository,
    private val webClient: WebClient,
    private val passwordEncoder: PasswordEncoder,
) {

    fun verifyAndGetUserInfo(provider: String, token: String): SocialUserInfo {
        return when (provider) {
            "kakao" -> verifyKakaoAccessToken(token)
            "naver" -> verifyNaverAccessToken(token)
            else -> throw CustomException(ErrorCode.INVALID_PROVIDER, "지원되지 않는 플랫폼의 소셜 로그인입니다.")
        }
    }

    private fun verifyKakaoAccessToken(accessToken: String): SocialUserInfo {
        // 카카오 사용자 정보 API 호출
        val responseBody = webClient.get()
            .uri("https://kapi.kakao.com/v2/user/me")
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) {
                it.bodyToMono(String::class.java)
                    .map { body -> CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Kakao token verify fail: $body") }
            }
            .bodyToMono(Map::class.java)
            .block()

        // 응답에서 id, email 추출
        val id = responseBody?.get("id")?.toString()
        val kakaoAccount = responseBody?.get("kakao_account") as? Map<*, *>
        val email = kakaoAccount?.get("email")?.toString()

        if (id == null || email == null) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Kakao id 또는 email을 찾을 수 없습니다.")
        }

        // SocialUserInfo 객체로 반환
        return SocialUserInfo("kakao", id, email)
    }

    private fun verifyNaverAccessToken(accessToken: String): SocialUserInfo {
        // 네이버 사용자 정보 API 호출
        val responseBody = webClient.get()
            .uri("https://openapi.naver.com/v1/nid/me")
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .onStatus({ status -> status.is4xxClientError || status.is5xxServerError }) {
                it.bodyToMono(String::class.java)
                    .map { body -> CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Naver token verify fail: $body") }
            }
            .bodyToMono(Map::class.java)
            .block()

        // 응답에서 id, email, name 추출
        val response = responseBody?.get("response") as? Map<*, *>
        val id = response?.get("id")?.toString()
        val email = response?.get("email")?.toString()

        if (id == null || email == null) {
            throw CustomException(ErrorCode.INVALID_SOCIAL_TOKEN, "Naver id 또는 email을 찾을 수 없습니다.")
        }

        // SocialUserInfo 객체로 반환
        return SocialUserInfo("naver", id, email)
    }

    fun createOrGetUser(socialUserInfo: SocialUserInfo): User {
        // 사용자 이름 생성
        val username = "${socialUserInfo.provider}_${socialUserInfo.providerId}"

        // 사용자 조회
        val user = userRepository.findByUsername(username)
        if (user != null) {
            return user
        }

        // 사용자 생성
        val newUser = User(
            username = username,
            password = null,
            roles = setOf("ROLE_USER")
        )
        return userRepository.save(newUser)
    }
}

