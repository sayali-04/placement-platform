package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Round;
import com.example.placement_platform.entity.RoundResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RoundRepository extends JpaRepository<Round, Long> {

    List<Round> findByApplicationIdOrderByScheduledAtAsc(Long applicationId);

    Optional<Round> findByIdAndApplicationUserEmail(Long id, String email);

    @Query("""
            select r.id from Round r
            where r.result = :result
              and r.scheduledAt > :windowStart
              and r.scheduledAt <= :windowEnd
            """)
    List<Long> findUpcomingIds(@Param("result") RoundResult result,
                               @Param("windowStart") Instant windowStart,
                               @Param("windowEnd") Instant windowEnd);
}