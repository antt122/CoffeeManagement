package com.example.hrservice.controller;

import com.example.hrservice.DTO.request.LeaveApprovalRequest;
import com.example.hrservice.DTO.request.LeaveRequestInput;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.LeaveRequestResponse;
import com.example.hrservice.enums.LeaveType;
import com.example.hrservice.service.LeaveRequestService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hr/leave-requests") // Base path
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    /**
     * API cho nhân viên tự nộp đơn xin nghỉ
     */
    @PostMapping("/apply")
    public ApiResponse<LeaveRequestResponse> applyForLeave(
            @RequestParam("leaveType") LeaveType leaveType,
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate,
            @RequestParam(value = "reason", required = false) String reason
    ) {
        // Bạn sẽ phải tự tạo DTO bên trong Controller
        LeaveRequestInput input = new LeaveRequestInput();
        input.setLeaveType(leaveType);
        input.setStartDate(startDate);
        input.setEndDate(endDate);
        input.setReason(reason);

        LeaveRequestResponse response = leaveRequestService.applyForLeave(input);
        return ApiResponse.<LeaveRequestResponse>builder()
                .result(response)
                .build();
    }

    /**
     * API cho Manager duyệt/từ chối đơn
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<LeaveRequestResponse> reviewLeaveRequest(
            @PathVariable String id,
            @RequestBody LeaveApprovalRequest input) {

        LeaveRequestResponse response = leaveRequestService.reviewLeaveRequest(id, input);
        return ApiResponse.<LeaveRequestResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping
    public ApiResponse<List<LeaveRequestResponse>> getMyLeaveRequests() {

        List<LeaveRequestResponse> response = leaveRequestService.getMyLeaveRequests();

        return ApiResponse.<List<LeaveRequestResponse>>builder()
                .result(response)
                .build();
    }

}