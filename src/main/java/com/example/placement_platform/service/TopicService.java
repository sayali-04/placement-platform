package com.example.placement_platform.service;



import com.example.placement_platform.dto.TopicRequest;
import com.example.placement_platform.dto.TopicResponse;
import com.example.placement_platform.entity.Topic;
import com.example.placement_platform.exception.ResourceNotFoundException;
import com.example.placement_platform.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public TopicResponse createTopic(TopicRequest request) {
        Topic topic = new Topic();
        topic.setName(request.name());
        topic.setDescription(request.description());
        Topic saved = topicRepository.save(topic);
        return toResponse(saved);
    }

    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TopicResponse getTopicById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));
        return toResponse(topic);
    }

    private TopicResponse toResponse(Topic topic) {
        return new TopicResponse(topic.getId(), topic.getName(), topic.getDescription());
    }

    public TopicResponse updateTopic(Long id, TopicRequest request) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id " + id));
        topic.setName(request.name());
        topic.setDescription(request.description());
        Topic saved = topicRepository.save(topic);
        return toResponse(saved);
    }

    public void deleteTopic(Long id) {
        if (!topicRepository.existsById(id)) {
            throw new ResourceNotFoundException("Topic not found with id " + id);
        }
        topicRepository.deleteById(id);
    }

}