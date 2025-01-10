package com.example.gava.domain.todo.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Icon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,              // 아이콘 이름
    val base64Data: String,        // Base64로 인코딩된 아이콘 데이터
    val contentType: String,       // MIME 타입 (예: image/png)
) {
    constructor() : this(null, "", "", "")
}
