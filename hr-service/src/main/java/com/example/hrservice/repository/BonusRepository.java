package com.example.hrservice.repository;

import com.example.hrservice.entity.Bonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, String> {
    List<Bonus> findByStaffIdOrderByDateDesc(String staffId);
}