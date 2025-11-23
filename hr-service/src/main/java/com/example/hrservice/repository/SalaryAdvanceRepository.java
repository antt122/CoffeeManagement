package com.example.hrservice.repository;

import com.example.hrservice.entity.SalaryAdvance;
import com.example.hrservice.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long> {

    List<SalaryAdvance> findByStaffId(String staffId);

    // Tìm các khoản ứng ĐÃ DUYỆT trong tháng cụ thể để trừ lương
    // (Logic: dateNeeded nằm trong khoảng start-end của tháng)
    @Query("SELECT s FROM SalaryAdvance s WHERE s.staffId = :staffId AND s.status = 'APPROVED' AND s.dateNeeded BETWEEN :startDate AND :endDate")
    List<SalaryAdvance> findApprovedAdvancesInMonth(String staffId, LocalDate startDate, LocalDate endDate);
}