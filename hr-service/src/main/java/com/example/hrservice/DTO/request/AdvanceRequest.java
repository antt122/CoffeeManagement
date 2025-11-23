package com.example.hrservice.DTO.request;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AdvanceRequest {
    private BigDecimal amount;
    private String reason;
    private LocalDate dateNeeded;
}