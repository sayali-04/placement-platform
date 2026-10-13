package com.example.placement_platform.dto;

import java.time.Instant;

public record AttemptResponse(
        Long id,
        Long questionId,
        String questionText,
        boolean correct,
        Instant attemptedAt
) {}