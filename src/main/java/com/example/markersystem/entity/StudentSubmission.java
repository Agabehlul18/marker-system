package com.example.markersystem.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.markersystem.enums.SubmissionStatus;

import java.util.ArrayList;
import java.util.List;

@Entity // BU ANNOTASİYA MƏTLƏQ OLMALIDIR
@Table(name = "student_submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String isNomresi;
    private String adSoyad;
    private String variant;
    private String sinif;
    private String otaq;
    private String yer;

    private Integer closedCorrectCount;
    private Double writtenTotalScore;
    private Double finalScore;

    @Enumerated(EnumType.STRING)
    private SubmissionStatus status;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WrittenQuestion> writtenQuestions = new ArrayList<>();
}