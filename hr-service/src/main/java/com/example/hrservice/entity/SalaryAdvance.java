package com.example.hrservice.entity;

import com.example.hrservice.enums.LeaveStatus; // Tái sử dụng Enum (PENDING/APPROVED/REJECTED)
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "salary_advances")
public class SalaryAdvance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(nullable = false)
    private BigDecimal amount; // Số tiền ứng

    private String reason;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING; // (Dùng tạm Enum LeaveStatus cho tiện)

    private LocalDate dateNeeded; // Ngày cần tiền

    @CreationTimestamp
    private LocalDateTime requestedAt;
}