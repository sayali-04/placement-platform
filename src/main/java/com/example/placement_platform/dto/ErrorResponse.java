package com.example.placement_platform.dto;


import java.time.Instant;

public record ErrorResponse(
        int status,
        String message,
        Instant timestamp
) {}