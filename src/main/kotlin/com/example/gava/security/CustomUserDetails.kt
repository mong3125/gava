package com.example.gava.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class CustomUserDetails(
    private val userId: Long,
    private val username: String,
    private val password: String,
    private val roles: List<GrantedAuthority>
) : UserDetails {

    override fun getAuthorities(): List<GrantedAuthority> = roles
    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true

    fun getUserId(): Long = userId // 커스텀 필드
}
