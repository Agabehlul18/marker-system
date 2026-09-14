package com.example.markersystem.service;

import com.example.markersystem.dto.ParsedSheetDto;
import com.example.markersystem.entity.AnswerKey;
import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.entity.WrittenQuestion;
import com.example.markersystem.enums.SubmissionStatus;
import com.example.markersystem.repository.AnswerKeyRepository;
import com.example.markersystem.repository.StudentSubmissionRepository;
import com.example.markersystem.repository.WrittenQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExamProcessingService {

    private final GeminiVisionService geminiVisionService;
    private final ImageCropService imageCropService;
    private final StudentSubmissionRepository submissionRepository;
    private final AnswerKeyRepository answerKeyRepository;
    private final WrittenQuestionRepository writtenQuestionRepository;

    @Transactional
    public StudentSubmission processStudentPages(File page1, File page2, String apiKey) throws Exception {
        // 1. Gemini vasitəsilə 1-ci üzü oxumaq (apiKey parametri ikinci arqument kimi ötürüldü)
        ParsedSheetDto parsedData = geminiVisionService.parseFrontPage(page1, apiKey);

        // Variantı təmizləyib standart formaya gətiririk
        String variant = (parsedData != null && parsedData.getVariant() != null)
                ? parsedData.getVariant().trim().toUpperCase()
                : "";

        // 2. Cavab açarını çəkmək və 1-21 suallar üzrə düzgünləri saymaq
        List<AnswerKey> keys = answerKeyRepository.findByVariant(variant);
        int correctClosedCount = calculateClosedCorrectCount(parsedData, keys);

        // 3. 22-25-ci sualları kəsib şəkilləri yaddaşda saxlamaq
        Map<String, String> cropPaths = imageCropService.cropStudentQuestions(
                page1,
                page2,
                parsedData != null ? parsedData.getIsNomresi() : null
        );

        if (cropPaths == null) {
            cropPaths = Collections.emptyMap();
        }

        // 4. Şagird imtahan qeydini yaratmaq
        StudentSubmission submission = StudentSubmission.builder()
                .isNomresi(parsedData != null ? parsedData.getIsNomresi() : null)
                .adSoyad(parsedData != null ? parsedData.getAdSoyad() : null)
                .variant(variant)
                .sinif(parsedData != null ? parsedData.getSinif() : null)
                .otaq(parsedData != null ? parsedData.getOtaq() : null)
                .yer(parsedData != null ? parsedData.getYer() : null)
                .closedCorrectCount(correctClosedCount)
                .writtenTotalScore(0.0)
                .finalScore(0.0)
                .status(SubmissionStatus.PENDING_MARKER)
                .build();

        StudentSubmission savedSubmission = submissionRepository.save(submission);

        // 5. 22, 23, 24, 25-ci sualları bazada formalaşdırmaq
        List<WrittenQuestion> writtenQuestions = new ArrayList<>();
        List<String> writtenQNumbers = List.of("22", "23", "24", "25");

        for (String qNum : writtenQNumbers) {
            WrittenQuestion wq = WrittenQuestion.builder()
                    .questionNumber(Integer.parseInt(qNum))
                    .imagePath(cropPaths.get(qNum))
                    .score(null) // Marker qiymətləndirənə qədər null saxlanılır
                    .submission(savedSubmission)
                    .build();
            writtenQuestions.add(wq);
        }

        List<WrittenQuestion> savedWrittenQuestions = writtenQuestionRepository.saveAll(writtenQuestions);
        savedSubmission.setWrittenQuestions(savedWrittenQuestions);

        return savedSubmission;
    }

    private int calculateClosedCorrectCount(ParsedSheetDto dto, List<AnswerKey> keys) {
        if (dto == null || keys == null || keys.isEmpty()) {
            return 0;
        }

        // Cavab açarlarını Fast Lookup üçün Map-ə yığırıq (Sual Nömrəsi -> Düzgün Cavab)
        Map<Integer, String> answerKeyMap = new HashMap<>();
        for (AnswerKey key : keys) {
            if (key != null && key.getQuestionNumber() != null && key.getCorrectAnswer() != null) {
                answerKeyMap.put(key.getQuestionNumber(), key.getCorrectAnswer().trim().toUpperCase());
            }
        }

        int totalCorrect = 0;

        // 1-15 qapalı suallar
        totalCorrect += countMatches(dto.getQapaliCavablar(), answerKeyMap);

        // 16-21 kodlaşdırılan açıq suallar
        totalCorrect += countMatches(dto.getAciqKodlasdirilan(), answerKeyMap);

        return totalCorrect;
    }

    private int countMatches(Map<String, String> studentAnswers, Map<Integer, String> answerKeyMap) {
        if (studentAnswers == null || studentAnswers.isEmpty()) {
            return 0;
        }

        int count = 0;

        for (Map.Entry<String, String> entry : studentAnswers.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }

            try {
                int qNum = Integer.parseInt(entry.getKey().trim());
                String studentAns = entry.getValue().trim().toUpperCase();

                if (!studentAns.isEmpty() && studentAns.equals(answerKeyMap.get(qNum))) {
                    count++;
                }
            } catch (NumberFormatException ignored) {
                // Şagird cavabında qeyri-rəqəm simvol gələrsə xəta vermədən keçir
            }
        }

        return count;
    }
}