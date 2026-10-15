package com.example.placement_platform.dto;

import java.time.Instant;

public record PrepQuestion(
        Long questionId,
        String questionText,
        String topicName,
        Instant lastAttemptedAt
) {}