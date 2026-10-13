package com.example.placement_platform.controller;

import com.example.placement_platform.dto.AttemptRequest;
import com.example.placement_platform.dto.AttemptResponse;
import com.example.placement_platform.dto.PageResponse;
import com.example.placement_platform.service.AttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @PostMapping
    public ResponseEntity<AttemptResponse> recordAttempt(Authentication authentication,
                                                         @Valid @RequestBody AttemptRequest request) {
        AttemptResponse created = attemptService.recordAttempt(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public PageResponse<AttemptResponse> getMyAttempts(Authentication authentication,
                                                       @RequestParam(defaultValue = "0") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        return attemptService.getMyAttempts(authentication.getName(), page, size);
    }
}