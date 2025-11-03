package com.example.hrservice.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI (Swagger) để định nghĩa Security Scheme (Bảo mật).
 * File này không cần @Bean hay mở rộng class, chỉ cần chú thích.
 */
@Configuration
@SecurityScheme(
        name = "api", // Tên của scheme (sẽ hiện trong nút Authorize)
        type = SecuritySchemeType.HTTP, // Loại bảo mật là HTTP
        bearerFormat = "JWT",           // Định dạng token là JWT
        scheme = "bearer"               // Tiền tố là "Bearer " (Bắt buộc cho JWT)
)
public class OpenApiConfig {
    // Không cần nội dung, chỉ cần chú thích
}
