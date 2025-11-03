package com.example.coffeemanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên vai trò, thường được định nghĩa theo chuẩn Spring Security
     * (bắt đầu bằng ROLE_...).
     */
    @Column(unique = true, nullable = false)
    private String name;

    // Bạn có thể thêm mô tả chi tiết hơn về vai trò này
    private String description;

    // Các vai trò gợi ý cho quán cà phê:
    // - ROLE_MANAGER (Quản lý)
    // - ROLE_CASHIER (Thu ngân)
    // - ROLE_BARISTA (Pha chế)
    // - ROLE_SERVER (Phục vụ)
    // - ROLE_ADMIN (Quản trị hệ thống - nên tách biệt nếu cần)
}
