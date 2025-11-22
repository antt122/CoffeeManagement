package com.example.hrservice.repository;

import com.example.hrservice.entity.StaffAllowance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StaffAllowanceRepository extends JpaRepository<StaffAllowance, Long> {
    List<StaffAllowance> findByStaffId(String staffId);
}