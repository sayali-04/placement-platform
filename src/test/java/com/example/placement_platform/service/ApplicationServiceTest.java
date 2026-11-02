package com.example.placement_platform.service;

import com.example.placement_platform.dto.ApplicationRequest;
import com.example.placement_platform.dto.ApplicationResponse;
import com.example.placement_platform.dto.StatusUpdateRequest;
import com.example.placement_platform.entity.Application;
import com.example.placement_platform.entity.ApplicationStatus;
import com.example.placement_platform.entity.User;
import com.example.placement_platform.exception.ConflictException;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.ApplicationRepository;
import com.example.placement_platform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Application applicationWithStatus(ApplicationStatus status) {
        Application application = new Application();
        application.setCompanyName("Infosys");
        application.setRoleTitle("Java Developer");
        application.setStatus(status);
        application.setCreatedAt(Instant.now());
        return application;
    }

    @Test
    void create_alwaysStartsAsApplied_andTrimsText() {
        when(userRepository.findByEmail("a@x.com")).thenReturn(Optional.of(new User()));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.create(
                "a@x.com", new ApplicationRequest("  TCS  ", " Developer "));

        assertThat(response.status()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.companyName()).isEqualTo("TCS");
        assertThat(response.roleTitle()).isEqualTo("Developer");
    }

    @Test
    void changeStatus_allowedMove_updatesAndSaves() {
        Application application = applicationWithStatus(ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUserEmail(1L, "a@x.com"))
                .thenReturn(Optional.of(application));
        when(applicationRepository.save(any(Application.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = applicationService.changeStatus(
                "a@x.com", 1L, new StatusUpdateRequest(ApplicationStatus.SHORTLISTED));

        assertThat(response.status()).isEqualTo(ApplicationStatus.SHORTLISTED);
        verify(applicationRepository).save(application);
    }

    @Test
    void changeStatus_forbiddenMove_throwsConflictAndSavesNothing() {
        Application application = applicationWithStatus(ApplicationStatus.REJECTED);
        when(applicationRepository.findByIdAndUserEmail(1L, "a@x.com"))
                .thenReturn(Optional.of(application));

        assertThatThrownBy(() -> applicationService.changeStatus(
                "a@x.com", 1L, new StatusUpdateRequest(ApplicationStatus.SHORTLISTED)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("REJECTED");

        verify(applicationRepository, never()).save(any());
    }

    @Test
    void changeStatus_applicationOfSomeoneElse_throwsNotFound() {
        when(applicationRepository.findByIdAndUserEmail(1L, "intruder@x.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.changeStatus(
                "intruder@x.com", 1L, new StatusUpdateRequest(ApplicationStatus.SHORTLISTED)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}