package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    boolean existsByReminderKey(String reminderKey);

    Page<Notification> findByUserEmail(String email, Pageable pageable);

    Optional<Notification> findByIdAndUserEmail(Long id, String email);

    long countByUserEmailAndReadFalse(String email);
}