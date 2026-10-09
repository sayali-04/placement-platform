package com.example.placement_platform.controller;

import com.example.placement_platform.dto.QuestionRequest;
import com.example.placement_platform.dto.QuestionResponse;
import com.example.placement_platform.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        QuestionResponse created = questionService.createQuestion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<QuestionResponse> getQuestions(@RequestParam(required = false) Long topicId) {
        if (topicId != null) {
            return questionService.getQuestionsByTopic(topicId);
        }
        return questionService.getAllQuestions();
    }

    @GetMapping("/{id}")
    public QuestionResponse getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id);
    }

    @PutMapping("/{id}")
    public QuestionResponse updateQuestion(@PathVariable Long id, @Valid @RequestBody QuestionRequest request) {
        return questionService.updateQuestion(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
