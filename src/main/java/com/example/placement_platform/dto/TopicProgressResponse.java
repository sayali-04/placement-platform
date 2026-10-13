package com.example.placement_platform.dto;

public record TopicProgressResponse(
        Long topicId,
        String topicName,
        long attempts,
        long correct,
        double accuracy,
        boolean weak
) {}
