package com.example.gava.domain.todo.dto

data class DataResponse (
    val data: ByteArray,
    val contentType: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataResponse) return false

        if (!data.contentEquals(other.data)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}