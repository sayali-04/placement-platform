package com.example.placement_platform.repository;

import com.example.placement_platform.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Page<Application> findByUserEmail(String email, Pageable pageable);

    Optional<Application> findByIdAndUserEmail(Long id, String email);
}