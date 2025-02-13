package com.example.gava.domain.user.entity

import com.example.gava.common.PrimaryKeyEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var username: String,   // 사용자 이름

    @Column
    var password: String? = null,

    // 사용자 권한(역할) 목록을 저장하기 위한 구성
    // "user_roles" 라는 별도 테이블에 user_id를 FK로 두고, 각 행에 role String을 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "role")
    var roles: Set<String> = emptySet(),

    @Column(name = "refresh_token")
    var refreshToken: String? = null
) : PrimaryKeyEntity()
