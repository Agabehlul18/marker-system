package com.example.markersystem.config;

import com.example.markersystem.entity.AnswerKey;
import com.example.markersystem.repository.AnswerKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AnswerKeyRepository answerKeyRepository;

    @Override
    public void run(String... args) {
        if (answerKeyRepository.count() == 0) {
            List<AnswerKey> keys = new ArrayList<>();

            // A Variantı cavab açarları (1-15 Qapalı, 16-21 Kodlaşdırılan)
            Map<Integer, String> variantAAnswers = Map.ofEntries(
                    Map.entry(1, "B"),
                    Map.entry(2, "D"),
                    Map.entry(3, "B"),
                    Map.entry(4, "A"),
                    Map.entry(5, "A"),
                    Map.entry(6, "E"),
                    Map.entry(7, "D"),
                    Map.entry(8, "D"),
                    Map.entry(9, "A"),
                    Map.entry(10, "D"),
                    Map.entry(11, "C"),
                    Map.entry(12, "B"),
                    Map.entry(13, "A"),
                    Map.entry(14, "B"),
                    Map.entry(15, "E"),
                    Map.entry(16, "24"),
                    Map.entry(17, "11"),
                    Map.entry(18, "2"),
                    Map.entry(19, "3"),
                    Map.entry(20, "4"),
                    Map.entry(21, "24")
            );

            // B Variantı cavab açarları (1-15 Qapalı, 16-21 Kodlaşdırılan)
            Map<Integer, String> variantBAnswers = Map.ofEntries(
                    Map.entry(1, "D"),
                    Map.entry(2, "D"),
                    Map.entry(3, "B"),
                    Map.entry(4, "B"),
                    Map.entry(5, "D"),
                    Map.entry(6, "E"),
                    Map.entry(7, "A"),
                    Map.entry(8, "D"),
                    Map.entry(9, "A"),
                    Map.entry(10, "C"),
                    Map.entry(11, "D"),
                    Map.entry(12, "B"),
                    Map.entry(13, "A"),
                    Map.entry(14, "B"),
                    Map.entry(15, "C"),
                    Map.entry(16, "28"),
                    Map.entry(17, "11"),
                    Map.entry(18, "3"),
                    Map.entry(19, "12"),
                    Map.entry(20, "1"),
                    Map.entry(21, "24")
            );

            variantAAnswers.forEach((qNum, ans) ->
                    keys.add(AnswerKey.builder().questionNumber(qNum).correctAnswer(ans).variant("A").build())
            );

            variantBAnswers.forEach((qNum, ans) ->
                    keys.add(AnswerKey.builder().questionNumber(qNum).correctAnswer(ans).variant("B").build())
            );

            answerKeyRepository.saveAll(keys);
            System.out.println(">>> MƏLUMAT: A və B variantlarının cavab açarları bazaya uğurla yazıldı!");
        }
    }
}
