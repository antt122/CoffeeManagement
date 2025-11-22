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
public class ApplicantHireRequest {
    // --- Thông tin Công việc (Bắt buộc) ---
    private String shopId;
    private BigDecimal salary;
    private LocalDate hireDate;
    private EmployeeType employeeType;

    // --- Thông tin Cá nhân (Nếu form ứng tuyển chưa có) ---
    // (Manager có thể cần điền nốt nếu form ban đầu không yêu cầu)
    private LocalDate dob;
    private String gender;

    // (Chúng ta sẽ lấy Tên, Email, SĐT từ hồ sơ ứng viên)
}