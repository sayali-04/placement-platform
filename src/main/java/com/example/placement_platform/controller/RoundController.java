package com.example.placement_platform.controller;

import com.example.placement_platform.dto.RescheduleRequest;
import com.example.placement_platform.dto.RoundRequest;
import com.example.placement_platform.dto.RoundResponse;
import com.example.placement_platform.dto.RoundResultRequest;
import com.example.placement_platform.service.RoundService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }

    @PostMapping("/applications/{applicationId}/rounds")
    public ResponseEntity<RoundResponse> addRound(Authentication authentication,
                                                  @PathVariable Long applicationId,
                                                  @Valid @RequestBody RoundRequest request) {
        RoundResponse created = roundService.addRound(authentication.getName(), applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/applications/{applicationId}/rounds")
    public List<RoundResponse> getRounds(Authentication authentication,
                                         @PathVariable Long applicationId) {
        return roundService.getRounds(authentication.getName(), applicationId);
    }

    @PatchMapping("/rounds/{roundId}/schedule")
    public RoundResponse reschedule(Authentication authentication,
                                    @PathVariable Long roundId,
                                    @Valid @RequestBody RescheduleRequest request) {
        return roundService.reschedule(authentication.getName(), roundId, request);
    }

    @PatchMapping("/rounds/{roundId}/result")
    public RoundResponse recordResult(Authentication authentication,
                                      @PathVariable Long roundId,
                                      @Valid @RequestBody RoundResultRequest request) {
        return roundService.recordResult(authentication.getName(), roundId, request);
    }
}