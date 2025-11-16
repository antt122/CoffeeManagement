package com.example.hrservice.DTO.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ProfileUpdateRequest {
    // Các trường nhân viên được phép tự sửa
    private String firstname; // (Có thể cho sửa tên)
    private String lastname;
    private LocalDate dob;
    private String gender;
    private String hometown;
    private String avatar; // (URL/Base64 của ảnh đại diện mới)
    private String bio;
}