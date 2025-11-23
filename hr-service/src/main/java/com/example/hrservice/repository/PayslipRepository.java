package com.example.hrservice.repository;

import com.example.hrservice.entity.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, String> {

    // Tìm phiếu lương của nhân viên trong 1 tháng cụ thể (tránh tạo trùng)
    Optional<Payslip> findByStaffIdAndSalaryPeriod(String staffId, LocalDate salaryPeriod);

    // Lấy lịch sử lương của nhân viên
    List<Payslip> findByStaffIdOrderBySalaryPeriodDesc(String staffId);
}