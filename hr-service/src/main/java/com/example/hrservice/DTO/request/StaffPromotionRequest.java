package com.example.hrservice.DTO.request;


import com.example.hrservice.enums.Status;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffPromotionRequest {
    // Trạng thái mới (ví dụ: ACTIVE)
    private Status newStatus;

    // Vai trò mới (ví dụ: ROLE_STAFF, ROLE_MANAGER)
    private String newRole;
}
