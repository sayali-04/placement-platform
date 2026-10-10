package com.example.placement_platform.dto;

import com.example.placement_platform.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) {}