package com.example.placement_platform.service;

import com.example.placement_platform.entity.Notification;
import com.example.placement_platform.entity.NotificationType;
import com.example.placement_platform.entity.Round;
import com.example.placement_platform.entity.RoundResult;
import com.example.placement_platform.repository.NotificationRepository;
import com.example.placement_platform.repository.RoundRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);

    private static final Duration FIRST_WINDOW = Duration.ofHours(24);
    private static final Duration FINAL_WINDOW = Duration.ofHours(2);
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private final RoundRepository roundRepository;
    private final NotificationRepository notificationRepository;

    public ReminderService(RoundRepository roundRepository,
                           NotificationRepository notificationRepository) {
        this.roundRepository = roundRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<Long> findUpcomingRoundIds(Instant now) {
        return roundRepository.findUpcomingIds(RoundResult.PENDING, now, now.plus(FIRST_WINDOW));
    }

    @Transactional
    public void createReminderIfDue(Long roundId) {
        Round round = roundRepository.findById(roundId).orElse(null);
        if (round == null || round.getResult() != RoundResult.PENDING) {
            return;
        }
        Instant now = Instant.now();
        Instant scheduledAt = round.getScheduledAt();
        if (!scheduledAt.isAfter(now)) {
            return;
        }
        Duration remaining = Duration.between(now, scheduledAt);
        if (remaining.compareTo(FIRST_WINDOW) > 0) {
            return;
        }

        String window = remaining.compareTo(FINAL_WINDOW) <= 0 ? "2h" : "24h";
        String key = "round-" + round.getId() + "-" + scheduledAt.toEpochMilli() + "-" + window;
        if (notificationRepository.existsByReminderKey(key)) {
            return;
        }

        Notification notification = new Notification();
        notification.setUser(round.getApplication().getUser());
        notification.setRound(round);
        notification.setType(NotificationType.ROUND_REMINDER);
        notification.setMessage(buildMessage(round, remaining));
        notification.setReminderKey(key);
        notification.setRead(false);
        notification.setCreatedAt(now);
        notificationRepository.save(notification);
        log.info("Created reminder {}", key);
    }

    private String buildMessage(Round round, Duration remaining) {
        String left = remaining.toMinutes() < 60
                ? "less than an hour"
                : "about " + Math.round(remaining.toMinutes() / 60.0) + " hours";
        String when = DISPLAY_FORMAT.format(round.getScheduledAt().atZone(DISPLAY_ZONE));
        return "Your " + round.getType().name() + " round at "
                + round.getApplication().getCompanyName()
                + " is in " + left + " (" + when + " IST). Check your prep list.";
    }
}