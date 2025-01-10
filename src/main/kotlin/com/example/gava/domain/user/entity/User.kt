package com.example.gava.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,   // 사용자 ID

    @Column(nullable = false, unique = true)
    val username: String,

    @Column(nullable = false)
    val password: String,

    // 사용자 권한(역할) 목록을 저장하기 위한 구성
    // "user_roles" 라는 별도 테이블에 user_id를 FK로 두고, 각 행에 role String을 저장
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    @Column(name = "role")
    val roles: Set<String> = emptySet(),

    val refreshToken: String? = null
) {
    constructor() : this(null, "", "", emptySet())
}
