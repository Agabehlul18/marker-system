//package com.example.markersystem.service;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@Slf4j
//public class ApiKeyManager {
//
//    // application.properties faylındakı siyahını avtomatik List-ə çevirir
//    @Value("${gemini.api.keys}")
//    private List<String> apiKeys;
//
//    private int currentIndex = 0;
//
//    public synchronized String getCurrentKey() {
//        if (apiKeys == null || apiKeys.isEmpty()) {
//            throw new IllegalStateException("API key tapılmadı! application.properties faylını yoxlayın.");
//        }
//        return apiKeys.get(currentIndex);
//    }
//
//    public synchronized String rotateKey() {
//        if (apiKeys == null || apiKeys.isEmpty()) {
//            throw new IllegalStateException("API key tapılmadı!");
//        }
//        currentIndex = (currentIndex + 1) % apiKeys.size();
//        log.warn(">>> API Key rotasiyası baş verdi. Yeni Key İndeksi: {}/{}", currentIndex + 1, apiKeys.size());
//        return getCurrentKey();
//    }
//}