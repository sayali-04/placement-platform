package com.example.placement_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotBlank @Size(max = 150) String companyName,
        @NotBlank @Size(max = 150) String roleTitle
) {}