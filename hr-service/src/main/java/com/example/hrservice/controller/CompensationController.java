package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.AllowanceRequest;
import com.example.hrservice.DTO.request.BonusRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.entity.Bonus;
import com.example.hrservice.entity.StaffAllowance;
import com.example.hrservice.service.CompensationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/compensation")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class CompensationController {

    private final CompensationService compensationService;

    // --- API PHỤ CẤP ---

    @PostMapping("/staff/{staffId}/allowances")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<StaffAllowance> addAllowance(
            @PathVariable String staffId,
            @RequestBody AllowanceRequest request) {
        return ApiResponse.<StaffAllowance>builder()
                .result(compensationService.addAllowance(staffId, request))
                .build();
    }

    @GetMapping("/staff/{staffId}/allowances")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<List<StaffAllowance>> getAllowances(@PathVariable String staffId) {
        return ApiResponse.<List<StaffAllowance>>builder()
                .result(compensationService.getAllowances(staffId))
                .build();
    }

    @DeleteMapping("/allowances/{allowanceId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<String> removeAllowance(@PathVariable Long allowanceId) {
        compensationService.removeAllowance(allowanceId);
        return ApiResponse.<String>builder().result("Deleted successfully").build();
    }

    // --- API THƯỞNG ---

    @PostMapping("/staff/{staffId}/bonuses")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<Bonus> addBonus(
            @PathVariable String staffId,
            @RequestBody BonusRequest request) {
        return ApiResponse.<Bonus>builder()
                .result(compensationService.addBonus(staffId, request))
                .build();
    }

    @GetMapping("/staff/{staffId}/bonuses")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ApiResponse<List<Bonus>> getBonuses(@PathVariable String staffId) {
        return ApiResponse.<List<Bonus>>builder()
                .result(compensationService.getBonuses(staffId))
                .build();
    }
}