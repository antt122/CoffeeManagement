package com.example.coffeemanager.DTO.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationEvent {
    private String staffId;
    private String username;
    private String password;
    private String role; // Tên của vai trò (ví dụ: "ROLE_STAFF")
}
