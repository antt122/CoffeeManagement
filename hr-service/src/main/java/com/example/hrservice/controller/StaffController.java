package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.InternalAccountRequest;
import com.example.hrservice.DTO.request.SalaryUpdateRequest;
import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.request.StaffPromotionRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.InternalAccountResponse;
import com.example.hrservice.DTO.response.SalaryHistoryResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.enums.Status;
import com.example.hrservice.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@RequestMapping("/api/hr/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
@Slf4j
public class StaffController {

    private final StaffService staffService;
    private final RestTemplate restTemplate;

    @Value("${services.auth.url}") // 👈 Tiêm URL của auth-service
    private String authServiceUrl;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<StaffResponse> createStaff(@RequestBody StaffCreationRequest request) {

        // 1. (MỚI) Gọi auth-service để tạo Account và lấy ID
        log.info("Gọi auth-service (từ StaffController) để tạo tài khoản...");
        InternalAccountRequest authRequest = new InternalAccountRequest();
        authRequest.setRole(request.getRole()); // Gửi Role

        ApiResponse<InternalAccountResponse> authResponse = restTemplate.postForObject(
                authServiceUrl + "/api/auth/internal/create-account",
                authRequest,
                ApiResponse.class
        );

        String newStaffId = authResponse.getResult().getStaffId();
        log.info("Auth-service đã tạo account, trả về staffId: {}", newStaffId);

        // 2. (MỚI) Gọi StaffService với ID đã được tạo
        // staffService.createStaff(request) -> staffService.createStaff(request, newStaffId)
        StaffResponse response = staffService.createStaff(request, newStaffId);

        return ApiResponse.<StaffResponse>builder()
                .result(response)
                .build();
    }

    @PutMapping("/{staffId}/promote")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<StaffResponse> promoteStaff(
            @PathVariable String staffId,
            @RequestBody StaffPromotionRequest request) {

        StaffResponse response = staffService.promoteStaff(staffId, request);

        return ApiResponse.<StaffResponse>builder()
                .result(response)
                .build();
    }
    @DeleteMapping("/{staffId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<StaffResponse> terminateStaff(@PathVariable String staffId) {

        StaffResponse response = staffService.terminateStaff(staffId);

        return ApiResponse.<StaffResponse>builder()
                .result(response)
                .build();
    }
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<List<StaffResponse>> getStaff(
            @RequestParam(required = true) Status status
    ) {
        List<StaffResponse> staffList = staffService.getStaffByStatus(status);

        return ApiResponse.<List<StaffResponse>>builder()
                .result(staffList)
                .build();
    }
    @PutMapping("/{staffId}/salary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<StaffResponse> updateSalary(
            @PathVariable String staffId, @RequestBody SalaryUpdateRequest request) {
        return ApiResponse.<StaffResponse>builder().result(staffService.updateSalary(staffId, request)).build();
    }

    @GetMapping("/{staffId}/salary-history")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<List<SalaryHistoryResponse>> getSalaryHistory(@PathVariable String staffId) {
        // Bạn cần triển khai getSalaryHistory trong StaffService trước
        return null; // (Placeholder)
    }
}