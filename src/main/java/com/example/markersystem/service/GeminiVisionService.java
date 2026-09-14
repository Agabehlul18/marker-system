package com.example.markersystem.service;

import com.example.markersystem.dto.ParsedSheetDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class GeminiVisionService {

    // Default val-ya boş string verilib ki, application.properties faylında olmasa Spring açılışda çökməsin
    @Value("${gemini.api.key:}")
    private String defaultApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Dinamik API Key qəbul edən əsas metod (ApiKeyManager rotasiyası üçün)
     */
    public ParsedSheetDto parseFrontPage(File page1File, String apiKey) throws Exception {
        String cleanKey = (apiKey != null && !apiKey.isBlank()) ? apiKey.trim() : defaultApiKey.trim();

        if (cleanKey.isBlank() || cleanKey.contains("SİZİN")) {
            throw new IllegalArgumentException("XƏTA: Keçərli Gemini API Key təyin edilməyib!");
        }

        byte[] imageBytes = Files.readAllBytes(page1File.toPath());
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        String prompt = """
            Bu cavab kartının şəklini oxu və YALNIZ aşağıdakı JSON strukturunda cavab qaytar (başqa heç bir mətn yazma):
            {
              "isNomresi": "şagirdin iş nömrəsi",
              "variant": "A və ya B",
              "sinif": "sinif nömrəsi",
              "otaq": "otaq nömrəsi",
              "yer": "yer nömrəsi",
              "adSoyad": "şagirdin ad soyad ata adı",
              "qapaliCavablar": {
                "1": "A", "2": "B", "3": "C", "4": "D", "5": "E",
                "6": "A", "7": "B", "8": "C", "9": "D", "10": "E",
                "11": "A", "12": "B", "13": "C", "14": "D", "15": "E"
              },
              "aciqKodlasdirilan": {
                "16": "14", "17": "3", "18": "3", "19": "12", "20": "1", "21": "18"
              }
            }
            """;

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of(
                                "parts", List.of(
                                        Map.of("text", prompt),
                                        Map.of("inline_data", Map.of(
                                                "mime_type", "image/png",
                                                "data", base64Image
                                        ))
                                )
                        )
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Google Gemini API tərəfindən rəsmən dəstəklənən real modellər
        String[] models = {"gemini-3.6-flash", "gemini-3.6-flash-8b"};

        for (String model : models) {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + cleanKey;

            for (int attempt = 1; attempt <= 2; attempt++) {
                try {
                    ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                    Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseMap.get("candidates");
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    String jsonText = (String) parts.get(0).get("text");

                    String cleanJson = jsonText.replace("```json", "").replace("```", "").strip();
                    return objectMapper.readValue(cleanJson, ParsedSheetDto.class);

                } catch (HttpServerErrorException.ServiceUnavailable e) {
                    System.err.println(">>> " + model + " 503 Xətası. (Model sıxlığı). Gözlənilir...");
                    Thread.sleep(2000);
                } catch (HttpStatusCodeException e) {
                    System.err.println(">>> GEMINI API CAVABI (" + model + "): " + e.getResponseBodyAsString());
                    throw e; // 429 xətasını BatchExamProcessingService tutub API Key-i dəyişsin deyə yuxarı ötürürük
                }
            }
        }

        throw new RuntimeException("Gemini serverləri hazırda həddindən artıq məşğuldur. Bir neçə dəqiqə sonra yenidən cəhd edin.");
    }

    /**
     * Overload metod: Tək parametr ötürüldükdə avtomatik default key istifadə edir
     */
    public ParsedSheetDto parseFrontPage(File page1File) throws Exception {
        return parseFrontPage(page1File, defaultApiKey);
    }
}