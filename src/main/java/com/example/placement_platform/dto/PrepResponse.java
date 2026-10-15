package com.example.placement_platform.dto;

import com.example.placement_platform.entity.RoundType;

import java.time.Instant;
import java.util.List;

public record PrepResponse(
        Long roundId,
        String companyName,
        RoundType roundType,
        Instant scheduledAt,
        long hoursLeft,
        PrepMode mode,
        List<TopicProgressResponse> weakTopics,
        List<PrepQuestion> questionsToRevise,
        List<String> checklist,
        String note
) {}