package com.example.placement_platform.dto;

import jakarta.validation.constraints.NotNull;

public record AttemptRequest(
        @NotNull Long questionId,
        @NotNull Boolean correct
) {}