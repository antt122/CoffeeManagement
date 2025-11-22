package com.example.hrservice.DTO.request;

import com.example.hrservice.enums.LeaveType;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LeaveRequestInput {
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
