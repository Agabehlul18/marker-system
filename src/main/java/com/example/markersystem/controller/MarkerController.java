package com.example.markersystem.controller;

import com.example.markersystem.dto.GradeRequestDto;
import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.service.MarkerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/marker")
public class MarkerController {

    private final MarkerService markerService;

    public MarkerController(MarkerService markerService) {
        this.markerService = markerService;
    }

    @GetMapping("/next")
    public ResponseEntity<StudentSubmission> getNext() {
        return markerService.getNextPendingSubmission()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/grade/{submissionId}")
    public ResponseEntity<String> gradeSubmission(
            @PathVariable Long submissionId,
            @RequestBody GradeRequestDto request) {
        markerService.submitGrades(submissionId, request);
        return ResponseEntity.ok("Şagirdin cavabları uğurla qiymətləndirildi!");
    }
}
