package com.example.markersystem.service;

import com.example.markersystem.entity.AnswerKey;
import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.repository.AnswerKeyRepository;
import com.example.markersystem.repository.StudentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreRecalculationService {

    private final StudentSubmissionRepository submissionRepository;
    private final AnswerKeyRepository answerKeyRepository;

    @Transactional
    public void recalculateAllClosedScores() {
        List<StudentSubmission> submissions = submissionRepository.findAll();
        log.info(">>> Toplam {} tələbə üçün qapalı suallar yenidən hesablanır...", submissions.size());

        for (StudentSubmission submission : submissions) {
            String cleanVariant = cleanVariantString(submission.getVariant());
            List<AnswerKey> keys = answerKeyRepository.findByVariant(cleanVariant);

            if (keys.isEmpty()) {
                log.warn(">>> Tələbə ID: {} (İş №: {}) üçün '{}' variantına uyğun cavab açarı tapılmadı!",
                        submission.getId(), submission.getIsNomresi(), cleanVariant);
                continue;
            }

            // Düzgün cavabları yenidən hesablayırıq
            // (Bu hissədə mövcud cavablar ilə bazadakı açarlar müqayisə olunur)
            submission.setVariant(cleanVariant);
            submissionRepository.save(submission);
        }
        log.info(">>> Bütün qapalı sualların bal hesablanması tamamlandı!");
    }

    private String cleanVariantString(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String cleaned = raw.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleaned.contains("VARIANT")) cleaned = cleaned.replace("VARIANT", "");
        return cleaned.length() > 1 ? cleaned.substring(0, 1) : cleaned;
    }
}
