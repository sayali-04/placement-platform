package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Attempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import com.example.placement_platform.dto.TopicStats;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.placement_platform.dto.PrepQuestion;

import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    @EntityGraph(attributePaths = "question")

    Page<Attempt> findByUserEmail(String email, Pageable pageable);

    @Query("""
        select new com.example.placement_platform.dto.TopicStats(
            t.id, t.name, count(a),
            sum(case when a.correct = true then 1L else 0L end))
        from Attempt a
        join a.question q
        join q.topic t
        where a.user.email = :email
        group by t.id, t.name
        having count(a) >= :minAttempts
        """)
    List<TopicStats> findTopicStats(@Param("email") String email,
                                    @Param("minAttempts") long minAttempts);

    @Query("""
        select new com.example.placement_platform.dto.PrepQuestion(
            q.id, q.questionText, t.name, a.attemptedAt)
        from Attempt a
        join a.question q
        join q.topic t
        where a.user.email = :email
          and a.correct = false
          and a.attemptedAt = (
              select max(a2.attemptedAt) from Attempt a2
              where a2.user = a.user and a2.question = a.question)
        order by a.attemptedAt desc
        """)
    List<PrepQuestion> findLatestWrong(@Param("email") String email, Pageable pageable);
}