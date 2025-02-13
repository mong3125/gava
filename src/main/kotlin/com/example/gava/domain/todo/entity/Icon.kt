package com.example.gava.domain.todo.entity

import com.example.gava.common.PrimaryKeyEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Lob

@Entity
class Icon(
    @Column(nullable = false)
    var name: String,              // 아이콘 이름

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMBLOB")
    val data: ByteArray,     // 원본 바이너리 아이콘 데이터

    val contentType: String,       // MIME 타입 (예: image/png)
) : PrimaryKeyEntity()