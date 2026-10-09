package com.example.placement_platform.controller;


import com.example.placement_platform.dto.TopicRequest;
import com.example.placement_platform.dto.TopicResponse;
import com.example.placement_platform.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping
    public ResponseEntity<TopicResponse> createTopic(@Valid @RequestBody TopicRequest request) {
        TopicResponse created = topicService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<TopicResponse> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GetMapping("/{id}")
    public TopicResponse getTopicById(@PathVariable Long id) {
        return topicService.getTopicById(id);
    }

    @PutMapping("/{id}")
    public TopicResponse updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return topicService.updateTopic(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}