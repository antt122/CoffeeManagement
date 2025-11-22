package com.example.hrservice.entity;

import com.example.hrservice.enums.EmployeeType;
import com.example.hrservice.enums.Position;
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
@Table(name = "position_salary_configs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"position", "employeeType"})
})
public class PositionSalaryConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeType employeeType;

    @Column(nullable = false)
    private BigDecimal baseSalary;
}
