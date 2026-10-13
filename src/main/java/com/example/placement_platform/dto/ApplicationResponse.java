package com.example.placement_platform.dto;

import com.example.placement_platform.entity.ApplicationStatus;

import java.time.Instant;

public record ApplicationResponse(
        Long id,
        String companyName,
        String roleTitle,
        ApplicationStatus status,
        Instant createdAt
) {}