package com.example.placement_platform.controller;

import com.example.placement_platform.dto.ApplicationRequest;
import com.example.placement_platform.dto.ApplicationResponse;
import com.example.placement_platform.dto.PageResponse;
import com.example.placement_platform.dto.StatusUpdateRequest;
import com.example.placement_platform.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> create(Authentication authentication,
                                                      @Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse created = applicationService.create(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public PageResponse<ApplicationResponse> getMine(Authentication authentication,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return applicationService.getMine(authentication.getName(), page, size);
    }

    @GetMapping("/{id}")
    public ApplicationResponse getOne(Authentication authentication, @PathVariable Long id) {
        return applicationService.getOne(authentication.getName(), id);
    }

    @PatchMapping("/{id}/status")
    public ApplicationResponse changeStatus(Authentication authentication,
                                            @PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest request) {
        return applicationService.changeStatus(authentication.getName(), id, request);
    }
}