package com.example.placement_platform.controller;

import com.example.placement_platform.dto.TopicProgressResponse;
import com.example.placement_platform.service.ProgressService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/topics")
    public List<TopicProgressResponse> getTopicProgress(
            Authentication authentication,
            @RequestParam(defaultValue = "3") int minAttempts) {
        return progressService.getTopicProgress(authentication.getName(), minAttempts);
    }
}