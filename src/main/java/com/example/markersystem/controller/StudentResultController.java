package com.example.markersystem.controller;

import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.repository.StudentSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentResultController {

    private final StudentSubmissionRepository submissionRepository;

    /**
     * Şagird iş nömrəsi ilə öz nəticəsini, yazılı sual şəkillərini və ballarını görür
     * Endpoint: GET /api/student/result/{isNomresi}
     */
    @GetMapping("/result/{isNomresi}")
    public ResponseEntity<StudentSubmission> getStudentResult(@PathVariable String isNomresi) {
        StudentSubmission submission = submissionRepository.findAll().stream()
                .filter(sub -> isNomresi.equals(sub.getIsNomresi()))
                .findFirst()
                .orElse(null);

        if (submission == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(submission);
    }
}