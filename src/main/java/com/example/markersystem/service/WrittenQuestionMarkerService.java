package com.example.markersystem.service;


import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.entity.WrittenQuestion;
import com.example.markersystem.enums.SubmissionStatus;
import com.example.markersystem.repository.StudentSubmissionRepository;
import com.example.markersystem.repository.WrittenQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WrittenQuestionMarkerService {

    private final WrittenQuestionRepository writtenQuestionRepository;
    private final StudentSubmissionRepository submissionRepository;

    /**
     * Marker müəllim tərəfindən daxil edilən balları qeyd edir
     * @param submissionId Tələbənin imtahan ID-si
     * @param questionScores Map<SualNömrəsi (22-25), Verilən Bal>
     */
    @Transactional
    public StudentSubmission gradeWrittenQuestions(Long submissionId, Map<Integer, Double> questionScores) {
        StudentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("İmtahan qeydi tapılmadı ID: " + submissionId));

        List<WrittenQuestion> questions = submission.getWrittenQuestions();
        double totalWrittenScore = 0.0;
        boolean allGraded = true;

        for (WrittenQuestion wq : questions) {
            if (questionScores.containsKey(wq.getQuestionNumber())) {
                Double score = questionScores.get(wq.getQuestionNumber());
                wq.setScore(score);
                writtenQuestionRepository.save(wq);
            }

            if (wq.getScore() != null) {
                totalWrittenScore += wq.getScore();
            } else {
                allGraded = false;
            }
        }

        // Yazılı sualların cəm balı
        submission.setWrittenTotalScore(totalWrittenScore);

        // Yekun Bal = Qapalı sualların balı + Yazılı sualların balı
        // (Hər qapalı sual 1 bal hesablanarsa; ehtiyac olduqda dərəcə əmsalı əlavə edilə bilər)
        double closedScore = submission.getClosedCorrectCount() != null ? submission.getClosedCorrectCount() : 0.0;
        submission.setFinalScore(closedScore + totalWrittenScore);

        // Bütün yazılı suallar qiymətləndirilibsə statusu YEKUNLAŞDIRIRIQ
        if (allGraded) {
            submission.setStatus(SubmissionStatus.COMPLETED);
        }

        return submissionRepository.save(submission);
    }
}
