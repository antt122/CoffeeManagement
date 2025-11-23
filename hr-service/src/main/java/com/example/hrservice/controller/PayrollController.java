package com.example.hrservice.controller;

import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.PayslipResponse;
import com.example.hrservice.service.PayrollService;
import com.example.hrservice.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/payroll")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class PayrollController {

    private final PayrollService payrollService;
    private final StaffService staffService;

    /**
     * API cho Manager tạo lương cho 1 nhân viên.
     * (Có tham số totalHours tùy chọn cho Part-time)
     */
    @PostMapping("/generate/{staffId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<PayslipResponse> generatePayslip(
            @PathVariable String staffId,
            @RequestParam(required = false) Double totalHours) {

        PayslipResponse response = payrollService.generatePayslip(staffId, totalHours);

        return ApiResponse.<PayslipResponse>builder()
                .result(response)
                .build();
    }

    /**
     * API cho Nhân viên tự xem lịch sử lương của mình
     */
    @GetMapping("/my-payslips")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<PayslipResponse>> getMyPayslips() {
        String staffId = staffService.getStaffIdFromToken();
        return ApiResponse.<List<PayslipResponse>>builder()
                .result(payrollService.getMyPayslips(staffId))
                .build();
    }
}