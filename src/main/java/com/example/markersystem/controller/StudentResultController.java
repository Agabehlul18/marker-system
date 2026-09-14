package com.example.markersystem.controller;

import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.repository.StudentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentResultController {

    private final StudentSubmissionRepository submissionRepository;

    /**
     * Şagird iş nömrəsi ilə öz nəticəsini görür və serverdə loqlanır
     * Endpoint: GET /api/student/result/{isNomresi}
     */
    @GetMapping("/result/{isNomresi}")
    public ResponseEntity<StudentSubmission> getStudentResult(@PathVariable String isNomresi) {
        StudentSubmission submission = submissionRepository.findAll().stream()
                .filter(sub -> isNomresi.equals(sub.getIsNomresi()))
                .findFirst()
                .orElse(null);

        if (submission == null) {
            log.warn("‼️Nəticə tapılmadı - Axtarılan iş nömrəsi: {}", isNomresi);
            return ResponseEntity.notFound().build();
        }

        // Server konsolunda görünəcək loq məlumatı
        log.info("✅Şagird nəticəyə baxdı -> İş nömrəsi: {}, Ad Soyad: {}, Variant: {}, Yekun Bal: {}",
                submission.getIsNomresi(),
                submission.getAdSoyad(),
                submission.getVariant(),
                submission.getFinalScore());

        return ResponseEntity.ok(submission);
    }
}