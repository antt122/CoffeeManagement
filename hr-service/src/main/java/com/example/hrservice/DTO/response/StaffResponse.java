package com.example.hrservice.DTO.response;
import com.example.hrservice.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffResponse {
    private String staffId;
    private String shopId;
    private Status status;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String jobTitle;

    // --- Thông tin Cá nhân (Đã cập nhật) ---
    private String firstname;
    private String lastname;

    // 👇 THÊM CÁC TRƯỜNG BỊ THIẾU VÀO ĐÂY
    private LocalDate dob;
    private String gender;
    private String hometown;
    private String avatar;
    private String bio;
}