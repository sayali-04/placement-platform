package com.example.placement_platform.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ReminderJob {

    private static final Logger log = LoggerFactory.getLogger(ReminderJob.class);

    private final ReminderService reminderService;

    public ReminderJob(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @Scheduled(initialDelay = 15_000, fixedDelay = 60_000)
    public void run() {
        List<Long> roundIds = reminderService.findUpcomingRoundIds(Instant.now());
        for (Long roundId : roundIds) {
            try {
                reminderService.createReminderIfDue(roundId);
            } catch (Exception ex) {
                log.warn("Could not create reminder for round {}: {}", roundId, ex.getMessage());
            }
        }
    }
}