package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.ApplicantHireRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.ApplicantResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.enums.ApplicantStatus;
import com.example.hrservice.service.ApplicantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/applicants") // 👈 Đường dẫn nội bộ
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class ApplicantManagementController {

    private final ApplicantService applicantService;

    /**
     * API để Manager/Admin xem danh sách ứng viên.
     * Lọc theo trạng thái, mặc định là PENDING.
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')") // 👈 Bảo vệ API
    public ApiResponse<List<ApplicantResponse>> getApplicants(
            @RequestParam(defaultValue = "PENDING") ApplicantStatus status
    ) {
        List<ApplicantResponse> applicants = applicantService.getApplicantsByStatus(status);

        return ApiResponse.<List<ApplicantResponse>>builder()
                .result(applicants)
                .build();
    }
    @PostMapping("/{applicantId}/hire")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<StaffResponse> hireApplicant(
            @PathVariable String applicantId,
            @RequestBody ApplicantHireRequest staffRequest // 👈 Manager điền thông tin nhân viên
    ) {
        StaffResponse staffResponse = applicantService.hireApplicant(applicantId, staffRequest);

        return ApiResponse.<StaffResponse>builder()
                .result(staffResponse)
                .build();
    }
}
