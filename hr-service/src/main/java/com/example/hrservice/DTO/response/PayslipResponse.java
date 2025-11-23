package com.example.hrservice.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayslipResponse {
    private String id;
    private String staffId;
    private String staffName; // Tên nhân viên (để in ra cho đẹp)
    private String jobTitle;
    private LocalDate salaryPeriod;

    // Chi tiết
    private BigDecimal baseSalary;
    private BigDecimal totalAllowances;
    private BigDecimal totalBonuses;
    private Double totalHoursWorked;
    private BigDecimal grossSalary;

    // Khấu trừ
    private BigDecimal healthInsurance;
    private BigDecimal unionFee;
    private BigDecimal totalDeductions;

    // Kết quả
    private BigDecimal netSalary;
    private boolean isPaid;
}