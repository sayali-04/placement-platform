package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Attempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    Page<Attempt> findByUserEmail(String email, Pageable pageable);
}