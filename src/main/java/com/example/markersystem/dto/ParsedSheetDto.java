package com.example.markersystem.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ParsedSheetDto {
    private String isNomresi;
    private String variant;
    private String sinif;
    private String otaq;
    private String yer;
    private String adSoyad;
    private Map<String, String> qapaliCavablar;      // 1-15 sualların variantları (məs: "1": "B")
    private Map<String, String> aciqKodlasdirilan;  // 16-21 sualların cavabları (məs: "16": "14")
}