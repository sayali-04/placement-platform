package com.example.placement_platform.entity;

public enum RoundResult {
    PENDING, CLEARED, FAILED;

    public boolean canMoveTo(RoundResult next) {
        return this == PENDING && (next == CLEARED || next == FAILED);
    }
}