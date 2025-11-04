package com.example.hrservice.repository;



import com.example.hrservice.entity.Staff;
import com.example.hrservice.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    List<Staff> findByStatusAndHireDateBeforeAndProbationNotifiedFalse(
            Status status,
            LocalDate ninetyDaysAgo
    );
}
