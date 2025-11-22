package com.example.hrservice.DTO.request;

import com.example.hrservice.enums.EmployeeType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffCreationRequest {
    // --- Thông tin Hồ sơ (Staff) ---
    private String firstname;
    private String lastname;
    private LocalDate dob;
    private String gender;
    private String shopId;
    private LocalDate hireDate;
    private BigDecimal salary;
    private String jobTitle;
    private String role; // Ví dụ: "ROLE_STAFF" hoặc "ROLE_MANAGER"
    private EmployeeType employeeType;
}
