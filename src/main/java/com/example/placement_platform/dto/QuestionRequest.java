package com.example.placement_platform.dto;

import com.example.placement_platform.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionRequest(
        @NotBlank String questionText,
        @NotBlank String answer,
        @NotNull Difficulty difficulty,
        @NotNull Long topicId
) {}
