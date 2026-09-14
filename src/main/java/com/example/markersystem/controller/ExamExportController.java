package com.example.markersystem.controller;

import com.example.markersystem.service.ExamManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/exam")
@RequiredArgsConstructor
public class ExamExportController {

    private final ExamManagementService examManagementService;

    /**
     * Bütün nəticələri Excel-də (CSV formatında) bir kliklə brauzerdən yükləmək üçün endpoint
     * Endpoint: GET /api/exam/export
     */
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExamResults() {
        try {
            String filePath = "imtahan_neticeleri.csv";
            String absolutePath = examManagementService.exportResultsToCsv(filePath);

            File file = new File(absolutePath);
            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=imtahan_neticeleri.csv");
            headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
            headers.add(HttpHeaders.PRAGMA, "no-cache");
            headers.add(HttpHeaders.EXPIRES, "0");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(file.length())
                    .contentType(MediaType.parseMediaType("application/csv;charset=UTF-8"))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
