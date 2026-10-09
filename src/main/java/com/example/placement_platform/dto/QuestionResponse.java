package com.example.placement_platform.dto;


import com.example.placement_platform.entity.Difficulty;

public record QuestionResponse(
        Long id,
        String questionText,
        String answer,
        Difficulty difficulty,
        Long topicId,
        String topicName
) {}