package com.example.placement_platform.service;

import com.example.placement_platform.dto.PrepMode;
import com.example.placement_platform.dto.PrepQuestion;
import com.example.placement_platform.dto.PrepResponse;
import com.example.placement_platform.dto.TopicProgressResponse;
import com.example.placement_platform.entity.Round;
import com.example.placement_platform.entity.RoundResult;
import com.example.placement_platform.entity.RoundType;
import com.example.placement_platform.exception.ConflictException;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.AttemptRepository;
import com.example.placement_platform.repository.RoundRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrepService {

    private static final int MIN_ATTEMPTS_FOR_WEAK_TOPIC = 3;

    private final RoundRepository roundRepository;
    private final AttemptRepository attemptRepository;
    private final ProgressService progressService;

    public PrepService(RoundRepository roundRepository,
                       AttemptRepository attemptRepository,
                       ProgressService progressService) {
        this.roundRepository = roundRepository;
        this.attemptRepository = attemptRepository;
        this.progressService = progressService;
    }

    @Transactional(readOnly = true)
    public PrepResponse getPrep(String email, Long roundId) {
        Round round = roundRepository.findByIdAndApplicationUserEmail(roundId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Round not found with id " + roundId));

        Instant now = Instant.now();
        if (round.getResult() != RoundResult.PENDING) {
            throw new ConflictException("This round is already finished");
        }
        if (!round.getScheduledAt().isAfter(now)) {
            throw new ConflictException("This round has already started or passed");
        }

        long hoursLeft = Duration.between(now, round.getScheduledAt()).toHours();
        PrepMode mode = chooseMode(hoursLeft);
        RoundType type = round.getType();

        List<TopicProgressResponse> weakTopics = List.of();
        List<PrepQuestion> questions = List.of();
        String note = null;

        boolean technical = type == RoundType.TECHNICAL
                || type == RoundType.CODING
                || type == RoundType.OTHER;

        if (technical) {
            if (mode != PrepMode.QUICK) {
                int topicLimit = mode == PrepMode.FOCUSED ? 3 : Integer.MAX_VALUE;
                weakTopics = progressService
                        .getTopicProgress(email, MIN_ATTEMPTS_FOR_WEAK_TOPIC).stream()
                        .filter(TopicProgressResponse::weak)
                        .limit(topicLimit)
                        .toList();
            }
            int questionLimit = mode == PrepMode.FULL ? 10 : 5;
            questions = attemptRepository.findLatestWrong(email, PageRequest.of(0, questionLimit));
            if (weakTopics.isEmpty() && questions.isEmpty()) {
                note = "No weak areas found yet. Practise more questions so the list can be personalised.";
            }
        } else {
            note = "Your question bank covers technical topics only, so this round gets a checklist.";
        }

        return new PrepResponse(
                round.getId(),
                round.getApplication().getCompanyName(),
                type,
                round.getScheduledAt(),
                hoursLeft,
                mode,
                weakTopics,
                questions,
                buildChecklist(type, mode),
                note);
    }

    private PrepMode chooseMode(long hoursLeft) {
        if (hoursLeft > 72) {
            return PrepMode.FULL;
        }
        if (hoursLeft >= 24) {
            return PrepMode.FOCUSED;
        }
        return PrepMode.QUICK;
    }

    private List<String> buildChecklist(RoundType type, PrepMode mode) {
        List<String> items = new ArrayList<>();
        switch (type) {
            case CODING -> {
                items.add("Revise arrays, strings, hashing and recursion basics");
                items.add("Practise writing code without an IDE");
                items.add("Check your laptop, internet and the test platform link");
            }
            case TECHNICAL -> {
                items.add("Be ready to explain every project on your resume");
                items.add("Revise Core Java: OOP, collections, exceptions");
                items.add("Revise SQL joins and normalization");
            }
            case HR -> {
                items.add("Prepare a one-minute introduction");
                items.add("Prepare answers on strengths, weaknesses and why this company");
                items.add("Prepare questions to ask the interviewer");
            }
            case APTITUDE -> {
                items.add("Practise quantitative, reasoning and verbal sections with a timer");
                items.add("Revise percentages, ratios and time-and-work");
                items.add("Check the test platform and ID requirements");
            }
            case OTHER -> {
                items.add("Confirm the format, date and time with the company");
            }
        }
        items.add("Keep your resume and documents ready");
        if (mode == PrepMode.QUICK) {
            items.add("Don't start new topics now. Review what you already know and rest well");
        }
        return items;
    }
}