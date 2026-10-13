package com.example.placement_platform.service;

import com.example.placement_platform.dto.ApplicationRequest;
import com.example.placement_platform.dto.ApplicationResponse;
import com.example.placement_platform.dto.PageResponse;
import com.example.placement_platform.dto.StatusUpdateRequest;
import com.example.placement_platform.entity.Application;
import com.example.placement_platform.entity.ApplicationStatus;
import com.example.placement_platform.entity.User;
import com.example.placement_platform.exception.ConflictException;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.ApplicationRepository;
import com.example.placement_platform.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse create(String email, ApplicationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Application application = new Application();
        application.setUser(user);
        application.setCompanyName(request.companyName().trim());
        application.setRoleTitle(request.roleTitle().trim());
        application.setStatus(ApplicationStatus.APPLIED);
        application.setCreatedAt(Instant.now());
        return toResponse(applicationRepository.save(application));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApplicationResponse> getMine(String email, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(
                applicationRepository.findByUserEmail(email, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getOne(String email, Long id) {
        return toResponse(findOwnedOrThrow(email, id));
    }

    @Transactional
    public ApplicationResponse changeStatus(String email, Long id, StatusUpdateRequest request) {
        Application application = findOwnedOrThrow(email, id);
        ApplicationStatus current = application.getStatus();
        ApplicationStatus next = request.status();
        if (!current.canMoveTo(next)) {
            throw new ConflictException("Cannot change status from " + current + " to " + next);
        }
        application.setStatus(next);
        return toResponse(applicationRepository.save(application));
    }

    private Application findOwnedOrThrow(String email, Long id) {
        return applicationRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id " + id));
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getCompanyName(),
                application.getRoleTitle(),
                application.getStatus(),
                application.getCreatedAt());
    }
}