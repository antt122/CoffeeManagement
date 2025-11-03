package com.example.coffeemanager.DTO.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class StaffRegistrationRequest {

    // --- Thông tin Account ---
    private String username;
    private String password;

    // --- Thông tin Staff (Hồ sơ) ---
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String gender;

    // --- Thông tin Công việc (Yêu cầu mới) ---
    private String shopId; // 👈 Mã cửa hàng
    private LocalDate hireDate; // Ngày vào làm
}
