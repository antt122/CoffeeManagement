package com.example.hrservice.entity;

import com.example.hrservice.enums.ApplicantStatus;
import com.example.hrservice.enums.Position;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "applicants")
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fullName;
    private String email;
    private String phone;
    @Enumerated(EnumType.STRING) // 👈 Báo cho JPA lưu tên (BARISTA) thay vì số (0)
    private Position positionApplied;

    @Lob // Dùng @Lob nếu bạn muốn lưu CV (dưới dạng Base64)
    private String cvUrl;

    private String coverLetter; // Thư xin việc

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicantStatus status = ApplicantStatus.PENDING;

    @Builder.Default
    private LocalDateTime appliedAt = LocalDateTime.now();
}
