package com.minibank;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Точка входа Spring Boot приложения Mini-Bank.
 * Spring IoC-контейнер автоматически сканирует пакет com.minibank,
 * регистрирует бины @Repository, @Service, @RestController и @RestControllerAdvice.
 */
@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Mini-Bank Spring REST API",
                version = "2.0.0",
                description = "REST API для управления банковскими счетами, переводами и финансовой аналитикой"
        )
)
public class MiniBankSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniBankSpringApplication.class, args);
    }
}
