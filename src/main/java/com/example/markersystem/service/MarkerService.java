package com.example.markersystem.service;

import com.example.markersystem.dto.GradeRequestDto;
import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.entity.WrittenQuestion;
import com.example.markersystem.enums.SubmissionStatus;
import com.example.markersystem.repository.StudentSubmissionRepository;
import com.example.markersystem.repository.WrittenQuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class MarkerService {

    private final StudentSubmissionRepository submissionRepo;
    private final WrittenQuestionRepository questionRepo;

    public MarkerService(StudentSubmissionRepository submissionRepo, WrittenQuestionRepository questionRepo) {
        this.submissionRepo = submissionRepo;
        this.questionRepo = questionRepo;
    }

    public Optional<StudentSubmission> getNextPendingSubmission() {
        return submissionRepo.findFirstByStatus(SubmissionStatus.PENDING_MARKER);
    }

    @Transactional
    public void submitGrades(Long submissionId, GradeRequestDto request) {
        StudentSubmission submission = submissionRepo.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("İş tapılmadı: " + submissionId));

        double totalWrittenScore = 0.0;

        for (Map.Entry<Long, Double> entry : request.questionScores().entrySet()) {
            WrittenQuestion question = questionRepo.findById(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Sual tapılmadı: " + entry.getKey()));

            double score = entry.getValue() != null ? entry.getValue() : 0.0;
            question.setScore(score);
            questionRepo.save(question);

            totalWrittenScore += score;
        }

        // 1. Əl ilə yoxlanılan yazılı sualların cəmi (22-25 suallar)
        totalWrittenScore = Math.round(totalWrittenScore * 100.0) / 100.0;
        submission.setWrittenTotalScore(totalWrittenScore);

        // 2. Qapalı və kodlaşdırılan sualların düzgün sayı (1-21 suallar)
        int closedCount = submission.getClosedCorrectCount() != null ? submission.getClosedCorrectCount() : 0;

        // 3. Yekun balın hesablanması: (qapalı + açıq * 2) * 100 / 29
        double rawScore = closedCount + (totalWrittenScore * 2.0);
        double calculatedFinalScore = (rawScore * 100.0) / 29.0;

        // Nəticəni 2 onluq işarəyə dəqiqləşdiririk (məsələn: 55.17)
        double roundedFinalScore = Math.round(calculatedFinalScore * 100.0) / 100.0;

        submission.setFinalScore(roundedFinalScore);
        submission.setStatus(SubmissionStatus.COMPLETED);

        submissionRepo.save(submission);
    }
}