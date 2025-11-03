package com.example.hrservice.DTO.event;


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
    private String password; // Gửi mật khẩu (chưa mã hóa) qua RabbitMQ
    private String role;     // Tên của vai trò (ví dụ: "ROLE_STAFF")
}
