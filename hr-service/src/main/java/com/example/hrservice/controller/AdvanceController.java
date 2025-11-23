package com.example.hrservice.controller;

import com.example.hrservice.DTO.request.AdvanceRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.entity.SalaryAdvance;
import com.example.hrservice.service.AdvanceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/advances")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class AdvanceController {

    private final AdvanceService advanceService;

    // Nhân viên xin ứng lương
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<SalaryAdvance> requestAdvance(@RequestBody AdvanceRequest request) {
        return ApiResponse.<SalaryAdvance>builder()
                .result(advanceService.requestAdvance(request))
                .build();
    }

    // Xem lịch sử ứng
    @GetMapping("/my-advances")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<SalaryAdvance>> getMyAdvances() {
        return ApiResponse.<List<SalaryAdvance>>builder()
                .result(advanceService.getMyAdvances())
                .build();
    }

    // Manager duyệt/từ chối
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<SalaryAdvance> approveAdvance(
            @PathVariable Long id, @RequestParam boolean approved) {
        return ApiResponse.<SalaryAdvance>builder()
                .result(advanceService.approveAdvance(id, approved))
                .build();
    }
}