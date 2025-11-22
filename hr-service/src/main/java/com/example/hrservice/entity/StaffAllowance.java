package com.example.hrservice.entity;

import com.example.hrservice.enums.AllowanceBasis;
import com.example.hrservice.enums.AllowanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "staff_allowances")
public class StaffAllowance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "staff_id", nullable = false)
    private String staffId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllowanceType allowanceType; // Loại (Ăn, Xe...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AllowanceBasis allowanceBasis; // Cách tính (Tháng/Giờ)

    @Column(nullable = false)
    private BigDecimal amount; // Giá trị (VD: 4000 hoặc 150000)
}