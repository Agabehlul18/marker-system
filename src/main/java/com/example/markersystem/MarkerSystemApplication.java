package com.example.markersystem;

import com.example.markersystem.entity.StudentSubmission;
import com.example.markersystem.entity.WrittenQuestion;
import com.example.markersystem.repository.StudentSubmissionRepository;
import com.example.markersystem.service.BatchExamProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class MarkerSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarkerSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(BatchExamProcessingService batchService,
                             StudentSubmissionRepository submissionRepository) {
        return args -> {
            try {
                System.out.println("\n================ 1. BATCH EMAL BAŞLADI ================");
                // Batch prosesi yenidən aktiv edildi ("abc" qovluğundakı şəkilləri oxuyur)
                batchService.processAllScansFromDirectory("abc");
                System.out.println(">>> Bütün varaqlar uğurla emal edildi və bazaya yazıldı!");

                System.out.println("\n================ 2. BAZADAKI İMTAHAN NƏTİCƏLƏRİ ================");
                List<StudentSubmission> submissions = submissionRepository.findAll();

                if (submissions.isEmpty()) {
                    System.out.println(">>> Bazada tələbə qeydi tapılmadı.");
                    return;
                }

                for (StudentSubmission sub : submissions) {
                    System.out.println("-------------------------------------------------------");
                    System.out.println("ID: " + sub.getId());
                    System.out.println("Ad Soyad: " + (sub.getAdSoyad() != null ? sub.getAdSoyad() : "N/A"));
                    System.out.println("İş Nömrəsi: " + sub.getIsNomresi());
                    System.out.println("Variant: " + sub.getVariant());
                    System.out.println("Düzgün Qapalı Cavab Sayı (1-21): " + sub.getClosedCorrectCount());
                    System.out.println("Status: " + sub.getStatus());
                    System.out.println("Yekun Bal: " + sub.getFinalScore());

                    System.out.println("Yazılı Cavab Şəkilləri (22-25):");
                    if (sub.getWrittenQuestions() != null) {
                        for (WrittenQuestion wq : sub.getWrittenQuestions()) {
                            System.out.println("   Sual " + wq.getQuestionNumber() +
                                    " -> Şəkil Path: " + wq.getImagePath() +
                                    " | Bal: " + (wq.getScore() != null ? wq.getScore() : "Hələ yazılmayıb"));
                        }
                    }
                }
                System.out.println("===============================================================\n");

            } catch (Exception e) {
                System.err.println(">>> XƏTA: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}