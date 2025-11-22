package com.example.hrservice.repository;


import com.example.hrservice.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    List<LeaveRequest> findByStaff_IdOrderByRequestedAtDesc(String staffId);
}
