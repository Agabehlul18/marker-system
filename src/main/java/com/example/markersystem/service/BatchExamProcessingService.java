package com.example.markersystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchExamProcessingService {

    private final ExamProcessingService examProcessingService;
    private final ApiKeyManager apiKeyManager;

    public void processAllScansFromDirectory(String directoryPath) {
        File folder = new File(directoryPath);

        if (!folder.exists() || !folder.isDirectory()) {
            log.warn(">>> XƏTA: '{}' qovluğu tapılmadı!", directoryPath);
            return;
        }

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg");
        });

        if (files == null || files.length == 0) {
            log.info(">>> MƏLUMAT: '{}' qovluğunda şəkil tapılmadı.", directoryPath);
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));

        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < files.length; i += 2) {
            File page1 = files[i];

            if (i + 1 >= files.length) {
                log.error(">>> XƏTA: '{}' üçün 2-ci səhifə faylı çatışmır (Tək fayl qaldı)!", page1.getName());
                failCount++;
                break;
            }

            File page2 = files[i + 1];

            boolean processedSuccessfully = false;
            int attempts = 0;
            int maxAttempts = 5;

            while (!processedSuccessfully && attempts < maxAttempts) {
                try {
                    String activeKey = apiKeyManager.getCurrentKey();

                    log.info(">>> Emal edilir: {} (Üz 1) və {} (Üz 2) | Key: {}",
                            page1.getName(), page2.getName(),
                            activeKey.substring(0, Math.min(activeKey.length(), 8)) + "...");

                    examProcessingService.processStudentPages(page1, page2, activeKey);

                    successCount++;
                    processedSuccessfully = true;

                    Thread.sleep(1000);

                } catch (Exception e) {
                    String errorMsg = e.getMessage() != null ? e.getMessage() : "";

                    if (errorMsg.contains("429") || errorMsg.contains("RESOURCE_EXHAUSTED")) {
                        attempts++;
                        log.warn(">>> 429 RATE LIMIT alındı! Növbəti API key-ə keçilir... (Cəhd {}/{})", attempts, maxAttempts);

                        apiKeyManager.rotateKey();

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        log.error(">>> XƏTA: {} və {} emal edilərkən problem yarandı: {}",
                                page1.getName(), page2.getName(), errorMsg);
                        break; // Rate limit xaricindəki xətalarda while-dan çıxırıq
                    }
                }
            }

            // Bütün cəhdlər bitdikdən sonra hələ də emal olunmayıbsa failCount artırılır
            if (!processedSuccessfully) {
                log.error(">>> XƏTA: {} və {} bütün API key-lər sınansa da emal edilə bilmədi!", page1.getName(), page2.getName());
                failCount++;
            }
        }

        log.info(">>> TOPLU EMAL TAMAMLANDI! Uğurlu Şagird Sayı: {}, Xətalı: {}", successCount, failCount);
    }
}