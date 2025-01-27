package com.example.gava.domain.todo.entity

import jakarta.persistence.*

@Entity
class Icon(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var name: String,              // 아이콘 이름

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    val data: ByteArray,     // 원본 바이너리 아이콘 데이터

    val contentType: String,       // MIME 타입 (예: image/png)
)