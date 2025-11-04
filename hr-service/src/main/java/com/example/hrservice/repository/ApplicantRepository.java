package com.example.hrservice.repository;

import com.example.hrservice.entity.Applicant;
import com.example.hrservice.enums.ApplicantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, String> {
    List<Applicant> findByStatus(ApplicantStatus status);
}