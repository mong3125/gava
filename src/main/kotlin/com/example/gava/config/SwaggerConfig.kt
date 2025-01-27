package com.example.gava.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("GAVA API Documentation")
                    .description("This is the GAVA API, providing endpoints for managing todos, user authentication, and more.")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("gava")
                            .email("gava@gmail.com")
                    )
            )
    }
}
