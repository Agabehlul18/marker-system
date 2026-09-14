package com.example.markersystem.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "written_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer questionNumber; // 22, 23, 24, 25
    private String imagePath;      // Kəsilmiş şəklin fayl yolu (məs: "crops/889510/q22.jpg")

    private Double score;          // 0.0, 0.33, 0.67, 1.0 (Marker müəllim daxil edəcək)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    @JsonIgnore
    @ToString.Exclude
    private StudentSubmission submission;
}
