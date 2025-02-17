package com.example.gava.apiLog

import com.example.gava.apiLog.data.ApiLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ApiLogRepository : JpaRepository<ApiLogEntity, Long>