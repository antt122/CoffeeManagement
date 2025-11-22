package com.example.hrservice.DTO.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BonusRequest {
    private BigDecimal amount;
    private String reason;
}