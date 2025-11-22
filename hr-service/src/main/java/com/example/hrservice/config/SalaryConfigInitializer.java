package com.example.hrservice.config;

import com.example.hrservice.entity.PositionSalaryConfig;
import com.example.hrservice.enums.EmployeeType;
import com.example.hrservice.enums.Position;
import com.example.hrservice.repository.PositionSalaryConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class SalaryConfigInitializer implements CommandLineRunner {

    private final PositionSalaryConfigRepository repository;

    @Override
    public void run(String... args) {
        log.info("Khởi tạo cấu hình lương (Fulltime & Parttime)...");
        createConfigIfNotFound(Position.BARISTA, EmployeeType.FULL_TIME, BigDecimal.valueOf(7000000));
        createConfigIfNotFound(Position.BARISTA, EmployeeType.PART_TIME, BigDecimal.valueOf(25000));
        createConfigIfNotFound(Position.CASHIER, EmployeeType.FULL_TIME, BigDecimal.valueOf(6500000));
        createConfigIfNotFound(Position.CASHIER, EmployeeType.PART_TIME, BigDecimal.valueOf(23000));
        createConfigIfNotFound(Position.MANAGER, EmployeeType.FULL_TIME, BigDecimal.valueOf(15000000));
    }

    private void createConfigIfNotFound(Position position, EmployeeType type, BigDecimal salary) {
        if (repository.findByPositionAndEmployeeType(position, type).isEmpty()) {
            repository.save(PositionSalaryConfig.builder()
                    .position(position)
                    .employeeType(type)
                    .baseSalary(salary)
                    .build());
        }
    }
}