package com.example.placement_platform.controller;

import com.example.placement_platform.dto.NotificationResponse;
import com.example.placement_platform.dto.PageResponse;
import com.example.placement_platform.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PageResponse<NotificationResponse> getMine(Authentication authentication,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return notificationService.getMine(authentication.getName(), page, size);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(Authentication authentication) {
        return Map.of("unread", notificationService.unreadCount(authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(Authentication authentication, @PathVariable Long id) {
        return notificationService.markRead(authentication.getName(), id);
    }
}