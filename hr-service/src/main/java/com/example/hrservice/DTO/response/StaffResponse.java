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
    private String firstname;
    private String lastname;
    private String shopId;
    private Status status;
    private BigDecimal salary;
    private LocalDate hireDate;
}