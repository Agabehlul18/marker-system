package com.example.markersystem.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ImageCropService {

    private static final String CROP_BASE_DIR = "crops/";

    /**
     * Şagirdin 1-ci və 2-ci üz şəkillərindən 22-25-ci sualları kəsir.
     *
     * @return Sualların fayl yollarını saxlayan Map (məs: "22" -> "crops/889510/q22.png")
     */
    public Map<String, String> cropStudentQuestions(File page1, File page2, String isNomresi) throws IOException {
        String studentDir = CROP_BASE_DIR + isNomresi + "/";
        File dir = new File(studentDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Map<String, String> cropPaths = new HashMap<>();

        // 1. Üz (9lar13092026_0001): Aşağıdakı 22-ci sualın çərçivəsi
        BufferedImage img1 = ImageIO.read(page1);
        String q22Path = studentDir + "q22.png";
        cropAndSave(img1, 0.05, 0.68, 0.90, 0.26, q22Path);
        cropPaths.put("22", q22Path);

        // 2. Üz (9lar13092026_0002): Yuxarı (23), Orta (24), Aşağı (25) suallar
        BufferedImage img2 = ImageIO.read(page2);

        String q23Path = studentDir + "q23.png";
        cropAndSave(img2, 0.05, 0.05, 0.90, 0.28, q23Path);
        cropPaths.put("23", q23Path);

        String q24Path = studentDir + "q24.png";
        cropAndSave(img2, 0.05, 0.34, 0.90, 0.29, q24Path);
        cropPaths.put("24", q24Path);

        String q25Path = studentDir + "q25.png";
        cropAndSave(img2, 0.05, 0.64, 0.90, 0.29, q25Path);
        cropPaths.put("25", q25Path);

        return cropPaths;
    }

    private void cropAndSave(BufferedImage src, double percentX, double percentY,
                             double percentW, double percentH, String outputPath) throws IOException {
        int x = (int) (src.getWidth() * percentX);
        int y = (int) (src.getHeight() * percentY);
        int w = (int) (src.getWidth() * percentW);
        int h = (int) (src.getHeight() * percentH);

        BufferedImage cropped = src.getSubimage(x, y, w, h);
        ImageIO.write(cropped, "png", new File(outputPath));
    }
}