package com.example.placement_platform.dto;

import com.example.placement_platform.entity.RoundType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record RoundRequest(
        @NotNull RoundType type,
        @NotNull @Future Instant scheduledAt
) {}