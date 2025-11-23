package com.example.hrservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payslips")
public class Payslip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    // Kỳ lương (Ví dụ: Tháng 11/2025 thì lưu ngày đầu tháng 2025-11-01)
    @Column(nullable = false)
    private LocalDate salaryPeriod;

    // --- THU NHẬP (INCOME) ---
    private BigDecimal baseSalary;        // Lương cơ bản (theo hợp đồng)
    private BigDecimal totalAllowances;   // Tổng phụ cấp
    private BigDecimal totalBonuses;      // Tổng thưởng
    private Double totalHoursWorked;      // Tổng giờ làm (đối với Part-time)

    @Column(nullable = false)
    private BigDecimal grossSalary;       // Tổng thu nhập trước thuế/khấu trừ

    // --- KHẤU TRỪ (DEDUCTIONS) ---
    private BigDecimal healthInsurance;   // BHYT (8%)
    private BigDecimal unionFee;          // Phí công đoàn (50k)
    // (Sau này có thể thêm BHXH, Thuế TNCN...)

    @Column(nullable = false)
    private BigDecimal totalDeductions;   // Tổng khấu trừ

    // --- THỰC NHẬN (NET) ---
    @Column(nullable = false)
    private BigDecimal netSalary;         // Tiền thực nhận về túi

    // --- TRẠNG THÁI ---
    private boolean isPaid;               // Đã chuyển khoản chưa?

    @CreationTimestamp
    private LocalDateTime createdAt;      // Ngày chốt lương

    private BigDecimal totalAdvances; // Tổng tiền đã ứng trong tháng
}