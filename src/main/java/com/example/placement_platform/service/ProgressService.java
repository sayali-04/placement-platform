package com.example.placement_platform.service;

import com.example.placement_platform.dto.TopicProgressResponse;
import com.example.placement_platform.dto.TopicStats;
import com.example.placement_platform.repository.AttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class ProgressService {

    private static final double WEAK_BELOW_PERCENT = 60.0;

    private final AttemptRepository attemptRepository;

    public ProgressService(AttemptRepository attemptRepository) {
        this.attemptRepository = attemptRepository;
    }

    @Transactional(readOnly = true)
    public List<TopicProgressResponse> getTopicProgress(String email, int minAttempts) {
        long min = Math.max(minAttempts, 1);
        return attemptRepository.findTopicStats(email, min).stream()
                .map(this::toResponse)
                .sorted(Comparator.comparingDouble(TopicProgressResponse::accuracy))
                .toList();
    }

    private TopicProgressResponse toResponse(TopicStats stats) {
        double accuracy = Math.round(1000.0 * stats.correct() / stats.attempts()) / 10.0;
        return new TopicProgressResponse(
                stats.topicId(),
                stats.topicName(),
                stats.attempts(),
                stats.correct(),
                accuracy,
                accuracy < WEAK_BELOW_PERCENT);
    }
}