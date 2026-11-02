package com.example.placement_platform.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlacementFlowIntegrationTest {

    private static final String PASSWORD = "Test@12345";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @BeforeEach
    void cleanDatabase() {
        assertThat(datasourceUrl).contains("placement_test");
        jdbcTemplate.execute("TRUNCATE TABLE notifications, attempts, rounds, applications, "
                + "questions, topics, users RESTART IDENTITY CASCADE");
    }

    // ---------- helpers ----------

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"" + email
                                + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String registerAndLogin(String email) throws Exception {
        register(email);
        return login(email);
    }

    private String adminToken() throws Exception {
        register("admin@example.com");
        jdbcTemplate.update("UPDATE users SET role = 'ADMIN' WHERE email = ?", "admin@example.com");
        return login("admin@example.com");
    }

    private long create(String url, String token, String json) throws Exception {
        String body = mockMvc.perform(post(url)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.id");
        return id.longValue();
    }

    private String questionJson(long topicId) {
        return "{\"questionText\":\"What is a List?\",\"answer\":\"An ordered collection\","
                + "\"difficulty\":\"EASY\",\"topicId\":" + topicId + "}";
    }

    // ---------- tests ----------

    @Test
    void register_storesHashedPassword_andNeverReturnsIt() throws Exception {
        String response = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sayali\",\"email\":\"a@example.com\",\"password\":\""
                                + PASSWORD + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain(PASSWORD).doesNotContain("passwordHash");

        String stored = jdbcTemplate.queryForObject(
                "SELECT password_hash FROM users WHERE email = ?", String.class, "a@example.com");
        assertThat(stored).startsWith("$2").isNotEqualTo(PASSWORD);
    }

    @Test
    void register_sameEmailTwice_returns409() throws Exception {
        register("dup@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Again\",\"email\":\"dup@example.com\",\"password\":\""
                                + PASSWORD + "\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void adminManagesContent_userCanOnlyRead() throws Exception {
        String admin = adminToken();
        String user = registerAndLogin("user@example.com");

        long topicId = create("/api/topics", admin,
                "{\"name\":\"Collections\",\"description\":\"List, Set, Map\"}");
        create("/api/questions", admin, questionJson(topicId));

        mockMvc.perform(get("/api/questions").header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].topicName").value("Collections"));

        mockMvc.perform(post("/api/topics")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void databaseConstraints_surfaceAsConflicts() throws Exception {
        String admin = adminToken();
        long topicId = create("/api/topics", admin, "{\"name\":\"SQL\"}");

        mockMvc.perform(post("/api/topics")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"SQL\"}"))
                .andExpect(status().isConflict());

        create("/api/questions", admin, questionJson(topicId));

        mockMvc.perform(delete("/api/topics/" + topicId)
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isConflict());
    }

    @Test
    void failedRound_rejectsTheApplication_andRoundsNeedShortlisting() throws Exception {
        String user = registerAndLogin("user@example.com");
        long applicationId = create("/api/applications", user,
                "{\"companyName\":\"TCS\",\"roleTitle\":\"Developer\"}");
        String roundJson = "{\"type\":\"TECHNICAL\",\"scheduledAt\":\""
                + Instant.now().plus(3, ChronoUnit.DAYS) + "\"}";

        mockMvc.perform(post("/api/applications/" + applicationId + "/rounds")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(roundJson))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/applications/" + applicationId + "/status")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SHORTLISTED\"}"))
                .andExpect(status().isOk());

        long roundId = create("/api/applications/" + applicationId + "/rounds", user, roundJson);

        mockMvc.perform(patch("/api/rounds/" + roundId + "/result")
                        .header("Authorization", bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"FAILED\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void attemptsAndApplications_arePrivateToTheirOwner() throws Exception {
        String admin = adminToken();
        long topicId = create("/api/topics", admin, "{\"name\":\"Java\"}");
        long questionId = create("/api/questions", admin, questionJson(topicId));
        String alice = registerAndLogin("alice@example.com");
        String bob = registerAndLogin("bob@example.com");

        create("/api/attempts", alice, "{\"questionId\":" + questionId + ",\"correct\":false}");
        long applicationId = create("/api/applications", alice,
                "{\"companyName\":\"Infosys\",\"roleTitle\":\"Developer\"}");

        mockMvc.perform(get("/api/attempts/me").header("Authorization", bearer(bob)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/attempts/me").header("Authorization", bearer(alice)))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/applications/" + applicationId)
                        .header("Authorization", bearer(bob)))
                .andExpect(status().isNotFound());
    }
}