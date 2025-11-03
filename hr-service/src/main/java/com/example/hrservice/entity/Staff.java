package com.example.hrservice.entity;

import com.example.hrservice.enums.Status;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "staffs") // Bảng "staffs" trong DB "coffee_hr_db"
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "staffId")
    private String id;

    // --- Thông tin Cá nhân ---
    String firstname;
    String lastname;
    LocalDate dob;
    String gender;
    String hometown;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String avatar;

    private String bio;

    // --- Thông tin Công việc (HR) ---
    @Enumerated(EnumType.STRING)
    Status status;

    LocalDate hireDate; // Ngày vào làm

    String shopId; // ID của chi nhánh/cửa hàng

    /**
     * Thông tin lương cơ bản của nhân viên.
     * Dùng BigDecimal để đảm bảo độ chính xác về tài chính.
     */
    @Column(precision = 19, scale = 2) // Ví dụ: 19 chữ số, 2 số sau dấu phẩy
    private BigDecimal salary;
}