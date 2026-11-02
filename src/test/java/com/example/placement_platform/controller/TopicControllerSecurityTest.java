package com.example.placement_platform.controller;

//import com.example.placement_platform.config.JwtAuthenticationFilter;
import com.example.placement_platform.config.SecurityConfig;
import com.example.placement_platform.dto.TopicResponse;
import com.example.placement_platform.entity.Role;
import com.example.placement_platform.entity.User;
import com.example.placement_platform.service.JwtService;
import com.example.placement_platform.service.TopicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TopicController.class,
        properties = {
                "app.jwt.secret=test-secret-key-that-is-at-least-forty-characters-long-1234567890",
                "app.jwt.expiration-minutes=60"
        })
@Import({SecurityConfig.class, JwtService.class})
class TopicControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private TopicService topicService;

    private String tokenFor(Role role) {
        User user = new User();
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setRole(role);
        return jwtService.generateToken(user);
    }

    @Test
    void getTopics_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/topics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTopics_asUser_returns200() throws Exception {
        when(topicService.getAllTopics())
                .thenReturn(List.of(new TopicResponse(1L, "Collections", "desc")));

        mockMvc.perform(get("/api/topics")
                        .header("Authorization", "Bearer " + tokenFor(Role.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Collections"));
    }

    @Test
    void createTopic_asUser_returns403_andNeverCallsService() throws Exception {
        mockMvc.perform(post("/api/topics")
                        .header("Authorization", "Bearer " + tokenFor(Role.USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring Boot\"}"))
                .andExpect(status().isForbidden());

        verify(topicService, never()).createTopic(any());
    }

    @Test
    void createTopic_asAdmin_returns201() throws Exception {
        when(topicService.createTopic(any()))
                .thenReturn(new TopicResponse(2L, "Spring Boot", null));

        mockMvc.perform(post("/api/topics")
                        .header("Authorization", "Bearer " + tokenFor(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring Boot\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void deleteTopic_asUser_returns403_andNeverCallsService() throws Exception {
        mockMvc.perform(delete("/api/topics/1")
                        .header("Authorization", "Bearer " + tokenFor(Role.USER)))
                .andExpect(status().isForbidden());

        verify(topicService, never()).deleteTopic(any());
    }

    @Test
    void deleteTopic_asAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/api/topics/1")
                        .header("Authorization", "Bearer " + tokenFor(Role.ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void getTopics_withTamperedToken_returns401() throws Exception {
        String token = tokenFor(Role.ADMIN);
        int signatureStart = token.lastIndexOf('.') + 1;
        char first = token.charAt(signatureStart);
        char replacement = first == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, signatureStart) + replacement
                + token.substring(signatureStart + 1);

        mockMvc.perform(get("/api/topics")
                        .header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTopics_withGarbageToken_returns401() throws Exception {
        mockMvc.perform(get("/api/topics")
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}