package com.example.hrservice.DTO.request;

import lombok.Data;
import java.math.BigDecimal;
@Data
public class SalaryUpdateRequest {
    private BigDecimal newSalary;
    private String reason;
}