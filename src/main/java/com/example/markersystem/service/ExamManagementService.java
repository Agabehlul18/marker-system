package com.example.markersystem.service;

import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.entity.WrittenQuestion;
import com.example.markersystem.enums.SubmissionStatus;
import com.example.markersystem.repository.StudentSubmissionRepository;
import com.example.markersystem.repository.WrittenQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamManagementService {

    private final StudentSubmissionRepository submissionRepository;
    private final WrittenQuestionRepository writtenQuestionRepository;

    /**
     * 1. Yazılı sualları (22-25) qiymətləndirmək üçün metod
     */
    @Transactional
    public StudentSubmission gradeWrittenQuestions(Long submissionId, Map<Integer, Double> questionScores) {
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Tələbə tapılmadı ID: " + submissionId));

        double writtenTotal = 0.0;
        boolean isAllGraded = true;

        for (WrittenQuestion wq : submission.getWrittenQuestions()) {
            if (questionScores.containsKey(wq.getQuestionNumber())) {
                Double score = questionScores.get(wq.getQuestionNumber());
                wq.setScore(score);
                writtenQuestionRepository.save(wq);
            }

            if (wq.getScore() != null) {
                writtenTotal += wq.getScore();
            } else {
                isAllGraded = false;
            }
        }

        submission.setWrittenTotalScore(writtenTotal);

        // Yekun Bal = Qapalı Cavab Sayı + Yazılı Sualların Balı
        double closedScore = submission.getClosedCorrectCount() != null ? submission.getClosedCorrectCount() : 0.0;
        submission.setFinalScore(closedScore + writtenTotal);

        if (isAllGraded) {
            submission.setStatus(SubmissionStatus.COMPLETED);
        }

        return submissionRepository.save(submission);
    }

    /**
     * 2. Bütün nəticələri Excel-də (CSV) eksport etmək üçün metod
     */
    public String exportResultsToCsv(String filePath) throws Exception {
        List<StudentSubmission> submissions = submissionRepository.findAll();
        File csvFile = new File(filePath);

        try (PrintWriter writer = new PrintWriter(csvFile, "UTF-8")) {
            // BOM əlavə olunur ki, Azərbaycan şriftləri Excel-də düzgün görünsün
            writer.write('\ufeff');
            writer.println("ID;İş Nömrəsi;Ad Soyad;Variant;Sinif;Qapalı Düz Sayı;Yazılı Balı;Yekun Bal;Status");

            for (StudentSubmission sub : submissions) {
                writer.printf("%d;%s;%s;%s;%s;%d;%.2f;%.2f;%s%n",
                        sub.getId(),
                        sub.getIsNomresi() != null ? sub.getIsNomresi() : "",
                        sub.getAdSoyad() != null ? sub.getAdSoyad() : "",
                        sub.getVariant() != null ? sub.getVariant() : "",
                        sub.getSinif() != null ? sub.getSinif() : "",
                        sub.getClosedCorrectCount() != null ? sub.getClosedCorrectCount() : 0,
                        sub.getWrittenTotalScore() != null ? sub.getWrittenTotalScore() : 0.0,
                        sub.getFinalScore() != null ? sub.getFinalScore() : 0.0,
                        sub.getStatus()
                );
            }
        }

        return csvFile.getAbsolutePath();
    }
}