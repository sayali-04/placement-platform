package com.example.placement_platform.controller;

import com.example.placement_platform.dto.PrepResponse;
import com.example.placement_platform.service.PrepService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rounds")
public class PrepController {

    private final PrepService prepService;

    public PrepController(PrepService prepService) {
        this.prepService = prepService;
    }

    @GetMapping("/{roundId}/prep")
    public PrepResponse getPrep(Authentication authentication, @PathVariable Long roundId) {
        return prepService.getPrep(authentication.getName(), roundId);
    }
}