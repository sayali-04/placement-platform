package com.example.placement_platform.dto;

import com.example.placement_platform.entity.RoundResult;
import com.example.placement_platform.entity.RoundType;

import java.time.Instant;

public record RoundResponse(
        Long id,
        Long applicationId,
        String companyName,
        RoundType type,
        Instant scheduledAt,
        RoundResult result
) {}