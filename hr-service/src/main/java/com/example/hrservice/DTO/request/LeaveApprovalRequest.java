package com.example.hrservice.DTO.request;

import com.example.hrservice.enums.LeaveStatus;
import lombok.Data;

@Data
public class LeaveApprovalRequest {
    private LeaveStatus newStatus; // APPROVED hoặc REJECTED
    private String managerNotes;
}
