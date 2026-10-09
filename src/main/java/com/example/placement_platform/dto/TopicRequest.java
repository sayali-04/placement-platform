package com.example.placement_platform.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicRequest(
        @NotBlank @Size(max = 100) String name,
        String description
) {}
