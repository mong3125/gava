package com.example.gava

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class GavaApplication

fun main(args: Array<String>) {
    runApplication<GavaApplication>(*args)
}
