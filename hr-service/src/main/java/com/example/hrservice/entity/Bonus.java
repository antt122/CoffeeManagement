package com.example.hrservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bonuses")
public class Bonus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Column(nullable = false)
    private BigDecimal amount; // Số tiền thưởng

    @Column(nullable = false)
    private String reason; // Lý do (VD: "Incentive tháng 11", "Thưởng tết")

    @CreationTimestamp
    private LocalDateTime date; // Ngày thưởng
}