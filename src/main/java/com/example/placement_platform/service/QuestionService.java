package com.example.placement_platform.service;

import com.example.placement_platform.dto.QuestionRequest;
import com.example.placement_platform.dto.QuestionResponse;
import com.example.placement_platform.entity.Question;
import com.example.placement_platform.entity.Topic;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.QuestionRepository;
import com.example.placement_platform.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TopicRepository topicRepository;

    public QuestionService(QuestionRepository questionRepository, TopicRepository topicRepository) {
        this.questionRepository = questionRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        Topic topic = findTopicOrThrow(request.topicId());
        Question question = new Question();
        question.setQuestionText(request.questionText());
        question.setAnswer(request.answer());
        question.setDifficulty(request.difficulty());
        question.setTopic(topic);
        return toResponse(questionRepository.save(question));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long id) {
        return toResponse(findQuestionOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByTopic(Long topicId) {
        findTopicOrThrow(topicId);
        return questionRepository.findByTopicId(topicId).stream().map(this::toResponse).toList();
    }

    @Transactional
    public QuestionResponse updateQuestion(Long id, QuestionRequest request) {
        Question question = findQuestionOrThrow(id);
        Topic topic = findTopicOrThrow(request.topicId());
        question.setQuestionText(request.questionText());
        question.setAnswer(request.answer());
        question.setDifficulty(request.difficulty());
        question.setTopic(topic);
        return toResponse(questionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found with id " + id);
        }
        questionRepository.deleteById(id);
    }

    private Question findQuestionOrThrow(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id " + id));
    }

    private Topic findTopicOrThrow(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));
    }

    private QuestionResponse toResponse(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getQuestionText(),
                question.getAnswer(),
                question.getDifficulty(),
                question.getTopic().getId(),
                question.getTopic().getName());
    }
}