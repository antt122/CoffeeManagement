package com.example.hrservice.DTO.response;
import com.example.hrservice.enums.Status;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class StaffResponse {
    private String staffId;
    private String firstname;
    private String lastname;
    private String shopId;
    private Status status;
    private BigDecimal salary;
    private LocalDate hireDate;
}