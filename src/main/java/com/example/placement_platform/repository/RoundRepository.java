package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Round;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {

    List<Round> findByApplicationIdOrderByScheduledAtAsc(Long applicationId);

    Optional<Round> findByIdAndApplicationUserEmail(Long id, String email);
}