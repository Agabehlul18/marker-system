package com.example.markersystem.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "answer_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String variant;        // "A" və ya "B"
    private Integer questionNumber; // 1-dən 21-ə qədər sual nömrəsi
    private String correctAnswer;   // "A", "B", "14", "18" və s.
}