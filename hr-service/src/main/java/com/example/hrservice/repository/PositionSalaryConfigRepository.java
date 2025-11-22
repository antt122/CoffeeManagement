package com.example.hrservice.repository;

import com.example.hrservice.entity.PositionSalaryConfig;
import com.example.hrservice.enums.EmployeeType;
import com.example.hrservice.enums.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PositionSalaryConfigRepository extends JpaRepository<PositionSalaryConfig, Long> {
    Optional<PositionSalaryConfig> findByPositionAndEmployeeType(Position position, EmployeeType employeeType);
}