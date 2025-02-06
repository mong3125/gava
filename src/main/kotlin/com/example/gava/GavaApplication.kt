package com.example.gava

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity

@SpringBootApplication
@EnableMethodSecurity
class GavaApplication

fun main(args: Array<String>) {
    runApplication<GavaApplication>(*args)
}
