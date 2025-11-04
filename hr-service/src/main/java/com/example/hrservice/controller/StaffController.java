package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.StaffCreationRequest;
import com.example.hrservice.DTO.request.StaffPromotionRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/hr/staff")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class StaffController {

    private final StaffService staffService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ApiResponse<StaffResponse> createStaff(@RequestBody StaffCreationRequest request) {

        StaffResponse response = staffService.createStaff(request);

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
}