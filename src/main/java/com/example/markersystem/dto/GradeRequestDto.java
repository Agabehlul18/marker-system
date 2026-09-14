package com.example.markersystem.dto;

import java.util.Map;

public record GradeRequestDto(
        Map<Long, Double> questionScores // Key: written_question_id, Value: score (məs: 0, 1.5, 2)
) {
}
