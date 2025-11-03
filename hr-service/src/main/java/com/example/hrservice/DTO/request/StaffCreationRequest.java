package com.example.hrservice.DTO.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StaffCreationRequest {
    // --- Thông tin Hồ sơ (Staff) ---
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String gender;
    private String shopId;
    private LocalDate hireDate;
    private BigDecimal salary;

    // --- Thông tin Tài khoản (Account) ---
    private String username;
    private String password;
    private String role; // Ví dụ: "ROLE_STAFF" hoặc "ROLE_MANAGER"
}
