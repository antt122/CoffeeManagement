package com.example.hrservice.DTO.response;

import com.example.hrservice.enums.ApplicantStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicantResponse {
    private String id;
    private String fullName;
    private String email;
    private String positionApplied;
    private ApplicantStatus status;
    private LocalDateTime appliedAt;
}
