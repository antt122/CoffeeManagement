package com.example.hrservice.DTO.request;

import com.example.hrservice.enums.AllowanceBasis;
import com.example.hrservice.enums.AllowanceType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AllowanceRequest {
    private AllowanceType allowanceType;
    private AllowanceBasis allowanceBasis;
    private BigDecimal amount;
}