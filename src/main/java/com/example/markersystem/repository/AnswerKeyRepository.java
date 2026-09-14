package com.example.markersystem.repository;


import com.example.markersystem.entity.AnswerKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerKeyRepository extends JpaRepository<AnswerKey, Long> {
    List<AnswerKey> findByVariant(String variant);
}