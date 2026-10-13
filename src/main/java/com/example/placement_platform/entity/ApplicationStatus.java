package com.example.placement_platform.entity;

public enum ApplicationStatus {
    APPLIED, SHORTLISTED, OFFERED, REJECTED, WITHDRAWN;

    public boolean canMoveTo(ApplicationStatus next) {
        return switch (this) {
            case APPLIED -> next == SHORTLISTED || next == REJECTED || next == WITHDRAWN;
            case SHORTLISTED -> next == OFFERED || next == REJECTED || next == WITHDRAWN;
            case OFFERED, REJECTED, WITHDRAWN -> false;
        };
    }
}