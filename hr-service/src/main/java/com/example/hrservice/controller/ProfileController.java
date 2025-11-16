package com.example.hrservice.controller;


import com.example.hrservice.DTO.request.ProfileUpdateRequest;
import com.example.hrservice.DTO.response.ApiResponse;
import com.example.hrservice.DTO.response.StaffResponse;
import com.example.hrservice.service.StaffService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hr") // Base path
@RequiredArgsConstructor
@SecurityRequirement(name = "api") // Yêu cầu JWT
public class ProfileController {

    private final StaffService staffService;

    /**
     * API để nhân viên tự lấy thông tin hồ sơ của mình.
     * Dữ liệu (staffId) được đọc từ JWT token.
     */
    @GetMapping("/my-info")
    // Mọi người (Staff, Manager, Admin) đều có thể tự xem thông tin
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StaffResponse> getMyInfo() {
        StaffResponse response = staffService.getMyInfo();
        return ApiResponse.<StaffResponse>builder()
                .result(response)
                .build();
    }
    @PutMapping("/my-info")
    @PreAuthorize("isAuthenticated()") // Bất kỳ ai đã đăng nhập
    public ApiResponse<StaffResponse> updateMyInfo(@RequestBody ProfileUpdateRequest request) {
        StaffResponse response = staffService.updateMyInfo(request);
        return ApiResponse.<StaffResponse>builder()
                .result(response)
                .build();
    }
}