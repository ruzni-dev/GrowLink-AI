package com.growlink.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "app.jwt.secret=VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class ProfileControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void profileCanBeCreatedAndRetrievedForAuthenticatedUser() throws Exception {
        String token = tokenFor(uniqueEmail());
        mockMvc.perform(put("/api/profiles/me").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"I help communities learn together.\",\"location\":\"Colombo\",\"country\":\"Sri Lanka\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bio").value("I help communities learn together."));
        mockMvc.perform(get("/api/profiles/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.location").value("Colombo"));
    }

    @Test
    void educationCrudIsScopedToAuthenticatedUser() throws Exception {
        String token = tokenFor(uniqueEmail());
        MvcResult created = mockMvc.perform(post("/api/profiles/me/education").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionName\":\"University of Colombo\",\"qualification\":\"BSc\",\"fieldOfStudy\":\"Computer Science\",\"startYear\":2020,\"endYear\":2024,\"currentlyStudying\":false}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.qualification").value("BSc")).andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(get("/api/profiles/me/education").header("Authorization", bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(delete("/api/profiles/me/education/" + id).header("Authorization", bearer(token))).andExpect(status().isOk());
        mockMvc.perform(get("/api/profiles/me/education").header("Authorization", bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void duplicateSkillsAreRejected() throws Exception {
        String token = tokenFor(uniqueEmail());
        String payload = "{\"name\":\"Facilitation\",\"category\":\"Education\",\"proficiencyLevel\":\"ADVANCED\",\"yearsOfExperience\":4}";
        mockMvc.perform(post("/api/profiles/me/skills").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isCreated());
        mockMvc.perform(post("/api/profiles/me/skills").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("This skill is already assigned to your profile"));
    }

    @Test
    void experienceAndInterestsCanBeManaged() throws Exception {
        String token = tokenFor(uniqueEmail());
        mockMvc.perform(post("/api/profiles/me/experience").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"jobTitle\":\"Community Mentor\",\"organization\":\"GrowLink\",\"employmentType\":\"Volunteer\",\"startDate\":\"2022-01-01\",\"currentlyWorking\":true}"))
                .andExpect(status().isCreated());
        MvcResult interests = mockMvc.perform(get("/api/profiles/interests").header("Authorization", bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(8))).andReturn();
        JsonNode available = objectMapper.readTree(interests.getResponse().getContentAsString());
        long first = available.get(0).get("id").asLong();
        mockMvc.perform(put("/api/profiles/me/interests").header("Authorization", bearer(token)).contentType(MediaType.APPLICATION_JSON).content("{\"interestIds\":[" + first + "]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/profiles/me/interests").header("Authorization", bearer(token))).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(first));
    }

    @Test
    void profileApisRejectAnonymousRequests() throws Exception {
        mockMvc.perform(get("/api/profiles/me")).andExpect(status().isUnauthorized());
    }

    private String tokenFor(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Profile Person\",\"email\":\"" + email + "\",\"password\":\"StrongPass1\",\"location\":\"Colombo\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());
        MvcResult result = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + email + "\",\"password\":\"StrongPass1\"}"))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String uniqueEmail() { return "profile-" + UUID.randomUUID() + "@example.com"; }
    private String bearer(String token) { return "Bearer " + token; }
}
