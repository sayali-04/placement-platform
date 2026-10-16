package com.example.placement_platform.repository;


import com.example.placement_platform.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import com.example.placement_platform.entity.Difficulty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @EntityGraph(attributePaths = "topic")
    List<Question> findByTopicId(Long topicId);

    @Query("""
        select q from Question q
        where (:topicId is null or q.topic.id = :topicId)
          and (:difficulty is null or q.difficulty = :difficulty)
          and lower(q.questionText) like :pattern
        """)
    Page<Question> search(@Param("topicId") Long topicId,
                          @Param("difficulty") Difficulty difficulty,
                          @Param("pattern") String pattern,
                          Pageable pageable);
}
