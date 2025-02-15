package com.example.gava.common

import CustomTsidFactory
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import org.hibernate.Hibernate
import org.springframework.data.domain.Persistable
import java.util.*

@MappedSuperclass
abstract class BaseEntity : Persistable<Long> {
    @Id
    private val id: Long = CustomTsidFactory.factory.create().toLong()

    @Transient
    private var _isNew = true

    override fun getId(): Long = id

    override fun isNew(): Boolean = _isNew

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false

        val thisClass = Hibernate.getClass(this)
        val otherClass = Hibernate.getClass(other)
        if (thisClass != otherClass) return false

        return id == (other as BaseEntity).id
    }

    override fun hashCode() = Objects.hashCode(id)

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }
}