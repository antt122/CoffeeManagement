package com.example.hrservice.DTO.response;

import com.example.hrservice.enums.LeaveStatus;
import com.example.hrservice.enums.LeaveType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestResponse {
    private String id;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private String managerNotes;
    private LocalDateTime requestedAt;
    private LeaveStatus status;
}
