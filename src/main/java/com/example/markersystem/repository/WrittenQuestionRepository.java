package com.example.markersystem.repository;


import com.example.markersystem.entity.WrittenQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WrittenQuestionRepository extends JpaRepository<WrittenQuestion, Long> {
    List<WrittenQuestion> findBySubmissionId(Long submissionId);

}