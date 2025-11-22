package com.example.hrservice.DTO.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryHistoryResponse {
    private String id;
    private String staffId;
    private BigDecimal oldSalary;
    private BigDecimal newSalary;
    private String reason;
    private String updatedBy;
    private LocalDateTime changedAt;
}
