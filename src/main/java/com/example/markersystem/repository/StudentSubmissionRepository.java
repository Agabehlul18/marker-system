package com.example.markersystem.repository;


import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentSubmissionRepository extends JpaRepository<StudentSubmission, Long> {
    Optional<StudentSubmission> findByIsNomresi(String isNomresi);
    Optional<StudentSubmission> findFirstByStatus(SubmissionStatus status);
}
