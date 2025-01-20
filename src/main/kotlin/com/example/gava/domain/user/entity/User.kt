package com.example.gava.domain.user.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,   // 사용자 ID

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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        return result
    }
}
