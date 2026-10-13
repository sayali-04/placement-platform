package com.example.placement_platform.service;

import com.example.placement_platform.dto.AttemptRequest;
import com.example.placement_platform.dto.AttemptResponse;
import com.example.placement_platform.dto.PageResponse;
import com.example.placement_platform.entity.Attempt;
import com.example.placement_platform.entity.Question;
import com.example.placement_platform.entity.User;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.AttemptRepository;
import com.example.placement_platform.repository.QuestionRepository;
import com.example.placement_platform.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;

    public AttemptService(AttemptRepository attemptRepository,
                          UserRepository userRepository,
                          QuestionRepository questionRepository) {
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    @Transactional
    public AttemptResponse recordAttempt(String email, AttemptRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id " + request.questionId()));

        Attempt attempt = new Attempt();
        attempt.setUser(user);
        attempt.setQuestion(question);
        attempt.setCorrect(request.correct());
        attempt.setAttemptedAt(Instant.now());
        return toResponse(attemptRepository.save(attempt));
    }

    @Transactional(readOnly = true)
    public PageResponse<AttemptResponse> getMyAttempts(String email, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize,
                Sort.by(Sort.Direction.DESC, "attemptedAt"));
        Page<Attempt> result = attemptRepository.findByUserEmail(email, pageable);
        return PageResponse.from(result.map(this::toResponse));
    }

    private AttemptResponse toResponse(Attempt attempt) {
        return new AttemptResponse(
                attempt.getId(),
                attempt.getQuestion().getId(),
                attempt.getQuestion().getQuestionText(),
                attempt.isCorrect(),
                attempt.getAttemptedAt());
    }
}