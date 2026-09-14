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

@Service
@RequiredArgsConstructor
public class MarkerEvaluationService {

    private final StudentSubmissionRepository submissionRepository;
    private final WrittenQuestionRepository writtenQuestionRepository;

    @Transactional
    public void evaluateQuestion(Long writtenQuestionId, Double score) {
        WrittenQuestion wq = writtenQuestionRepository.findById(writtenQuestionId)
                .orElseThrow(() -> new RuntimeException("Sual tapılmadı!"));

        wq.setScore(score);
        writtenQuestionRepository.save(wq);

        StudentSubmission submission = wq.getSubmission();
        checkAndUpdateFinalScore(submission);
    }

    private void checkAndUpdateFinalScore(StudentSubmission submission) {
        List<WrittenQuestion> questions = writtenQuestionRepository.findBySubmissionId(submission.getId());

        // Əgər 22-25 suallarının hər birinə bal verilibsə:
        boolean allGraded = questions.stream().allMatch(q -> q.getScore() != null);

        if (allGraded) {
            double totalWrittenScore = questions.stream()
                    .mapToDouble(WrittenQuestion::getScore)
                    .sum();

            // Yekun balın düsturla hesablanması:
            // ((1-21 düz sayı) + (yazı işi balı * 2)) * 100 / 29
            double rawScore = submission.getClosedCorrectCount() + (totalWrittenScore * 2.0);
            double finalScore = (rawScore * 100.0) / 29.0;

            // Vergüldən sonra 2 rəqəmə qədər yuvarlaqlaşdırmaq
            double roundedFinal = Math.round(finalScore * 100.0) / 100.0;

            submission.setWrittenTotalScore(totalWrittenScore);
            submission.setFinalScore(roundedFinal);
            submission.setStatus(SubmissionStatus.COMPLETED);

            submissionRepository.save(submission);
        }
    }
}
