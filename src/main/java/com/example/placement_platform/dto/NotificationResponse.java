package com.example.placement_platform.dto;

import com.example.placement_platform.entity.NotificationType;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        Long roundId,
        NotificationType type,
        String message,
        boolean read,
        Instant createdAt
) {}