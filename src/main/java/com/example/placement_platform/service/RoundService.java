package com.example.placement_platform.service;

import com.example.placement_platform.dto.RescheduleRequest;
import com.example.placement_platform.dto.RoundRequest;
import com.example.placement_platform.dto.RoundResponse;
import com.example.placement_platform.dto.RoundResultRequest;
import com.example.placement_platform.entity.Application;
import com.example.placement_platform.entity.ApplicationStatus;
import com.example.placement_platform.entity.Round;
import com.example.placement_platform.entity.RoundResult;
import com.example.placement_platform.exception.ConflictException;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.ApplicationRepository;
import com.example.placement_platform.repository.RoundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoundService {

    private final RoundRepository roundRepository;
    private final ApplicationRepository applicationRepository;

    public RoundService(RoundRepository roundRepository,
                        ApplicationRepository applicationRepository) {
        this.roundRepository = roundRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public RoundResponse addRound(String email, Long applicationId, RoundRequest request) {
        Application application = findOwnedApplicationOrThrow(email, applicationId);
        if (application.getStatus() != ApplicationStatus.SHORTLISTED) {
            throw new ConflictException("Rounds can only be added to a SHORTLISTED application");
        }
        Round round = new Round();
        round.setApplication(application);
        round.setType(request.type());
        round.setScheduledAt(request.scheduledAt());
        round.setResult(RoundResult.PENDING);
        return toResponse(roundRepository.save(round));
    }

    @Transactional(readOnly = true)
    public List<RoundResponse> getRounds(String email, Long applicationId) {
        findOwnedApplicationOrThrow(email, applicationId);
        return roundRepository.findByApplicationIdOrderByScheduledAtAsc(applicationId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RoundResponse reschedule(String email, Long roundId, RescheduleRequest request) {
        Round round = findOwnedRoundOrThrow(email, roundId);
        if (round.getResult() != RoundResult.PENDING) {
            throw new ConflictException("Only a PENDING round can be rescheduled");
        }
        round.setScheduledAt(request.scheduledAt());
        return toResponse(roundRepository.save(round));
    }

    @Transactional
    public RoundResponse recordResult(String email, Long roundId, RoundResultRequest request) {
        Round round = findOwnedRoundOrThrow(email, roundId);
        RoundResult current = round.getResult();
        RoundResult next = request.result();
        if (!current.canMoveTo(next)) {
            throw new ConflictException("Cannot change round result from " + current + " to " + next);
        }
        round.setResult(next);

        if (next == RoundResult.FAILED) {
            Application application = round.getApplication();
            if (application.getStatus().canMoveTo(ApplicationStatus.REJECTED)) {
                application.setStatus(ApplicationStatus.REJECTED);
                applicationRepository.save(application);
            }
        }
        return toResponse(roundRepository.save(round));
    }

    private Application findOwnedApplicationOrThrow(String email, Long id) {
        return applicationRepository.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id " + id));
    }

    private Round findOwnedRoundOrThrow(String email, Long id) {
        return roundRepository.findByIdAndApplicationUserEmail(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found with id " + id));
    }

    private RoundResponse toResponse(Round round) {
        return new RoundResponse(
                round.getId(),
                round.getApplication().getId(),
                round.getApplication().getCompanyName(),
                round.getType(),
                round.getScheduledAt(),
                round.getResult());
    }
}