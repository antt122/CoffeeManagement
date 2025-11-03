package com.example.coffeemanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Setter
@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts") // Bảng cho thông tin đăng nhập
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "accountId")
    private String id;

    // --- Thông tin Đăng nhập & Bảo mật ---
    @Column(unique = true, nullable = false)
    String username; // Tên đăng nhập

    @Column(nullable = false)
    String password; // Mật khẩu (đã mã hóa)

    // Khóa ngoại liên kết tới thông tin hồ sơ của nhân viên (Entity Staff)
    // Trường này giúp biết Account này thuộc về Staff nào
    @Column(unique = true) // Đảm bảo 1 Account chỉ liên kết với 1 Staff
            String staffId;

    // Trạng thái tài khoản (Active/Inactive)
    @Builder.Default
    Boolean enabled = true;

    // --- Phân quyền (Authorization) ---
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_roles",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    Set<Role> roles;
}