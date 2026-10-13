package com.example.placement_platform.dto;

import com.example.placement_platform.entity.RoundResult;
import jakarta.validation.constraints.NotNull;

public record RoundResultRequest(@NotNull RoundResult result) {}