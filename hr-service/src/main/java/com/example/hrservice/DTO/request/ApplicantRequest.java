package com.example.hrservice.DTO.request;

import com.example.hrservice.enums.Position;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicantRequest {
    private String fullName;
    private String email;
    private String phone;
    private Position positionApplied;
    private String cvData; // (Base64 string)
    private String coverLetter;
}
