package com.example.hrservice.DTO.request;

import lombok.Data;

@Data
public class InternalAccountRequest {
    private String role; // Chỉ cần Role, vì username/password sẽ tự tạo
}