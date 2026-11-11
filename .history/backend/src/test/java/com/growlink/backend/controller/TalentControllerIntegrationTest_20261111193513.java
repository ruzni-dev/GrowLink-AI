package com.growlink.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(properties = {
        "app.jwt.secret=VGhpcy1pcy1hLXRlc3Qta2V5LXRoYXQtaXMtbG9uZy1lbm91Z2gtZm9yLUhTMjU2",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class TalentControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @Test
    void publicProfileCanBeSearchedAnonymouslyWithoutPrivateFields() throws Exception {
        createTalent("Amara Fernando", "Colombo", "Java", "TECHNOLOGY_DIGITAL_SKILLS", "ADVANCED", 5, "Computer Science", true);

        mockMvc.perform(get("/api/talents/search")
                        .param("name", "Amara")
                        .param("skill", "Java")
                        .param("category", "TECHNOLOGY_DIGITAL_SKILLS")
                        .param("location", "Colombo")
                        .param("proficiencyLevel", "ADVANCED")
                        .param("minimumExperience", "3")
                        .param("education", "Computer")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].fullName").value("Amara Fernando"))
                .andExpect(jsonPath("$.content[0].skills[0].proficiencyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.content[0].email").doesNotExist())
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].password").doesNotExist());

        mockMvc.perform(get("/api/talents/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void privateAndInactiveProfilesAreExcluded() throws Exception {
        createTalent("Private Person", "Kandy", "Java", "TECHNOLOGY_DIGITAL_SKILLS", "INTERMEDIATE", 2, "Computing", false);
        TalentFixture inactive = createTalent("Inactive Person", "Galle", "Coaching", "SPORTS_COACHING", "EXPERT", 8, "Sports Science", true);
        User user = userRepository.findByEmailIgnoreCase(inactive.email()).orElseThrow();
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        mockMvc.perform(get("/api/talents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));

        mockMvc.perform(get("/api/talents/" + inactive.id()))
                .andExpect(status().isNotFound());
    }

    @Test
    void paginationSortingAndEmptyResultsWork() throws Exception {
        createTalent("Zara Perera", "Colombo", "Design", "ARTS_CULTURE", "INTERMEDIATE", 3, "Visual Arts", true);
        createTalent("Anil Silva", "Matara", "Mentoring", "MENTORSHIP_GUIDANCE", "EXPERT", 9, "Education", true);

        mockMvc.perform(get("/api/talents/search").param("sortBy", "name").param("page", "0").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].fullName").value("Anil Silva"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));

        mockMvc.perform(get("/api/talents/search").param("skill", "NoSuchSkill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void categoriesEndpointReturnsCanonicalTaxonomy() throws Exception {
        mockMvc.perform(get("/api/talents/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)))
                .andExpect(jsonPath("$[0].label").value("Education & Learning"));
    }

    private TalentFixture createTalent(String name, String location, String skill, String category, String level,
            int years, String qualification, boolean isPublic) throws Exception {
        String email = "talent-" + UUID.randomUUID() + "@example.com";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"StrongPass1\",\"location\":\"" + location + "\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"StrongPass1\"}"))
                .andExpect(status().isOk()).andReturn();
        JsonNode loginBody = objectMapper.readTree(login.getResponse().getContentAsString());
        String token = loginBody.get("accessToken").asText();
        String bearer = "Bearer " + token;

        mockMvc.perform(put("/api/profiles/me").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Experienced community contributor.\",\"location\":\"" + location
                                + "\",\"visibility\":\"" + (isPublic ? "PUBLIC" : "PRIVATE") + "\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/profiles/me/skills").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + skill + "\",\"category\":\"" + category
                                + "\",\"proficiencyLevel\":\"" + level + "\",\"yearsOfExperience\":" + years + "}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/profiles/me/education").header("Authorization", bearer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"institutionName\":\"Community Institute\",\"qualification\":\"" + qualification
                                + "\",\"startYear\":2018,\"endYear\":2022,\"currentlyStudying\":false}"))
                .andExpect(status().isCreated());

        return new TalentFixture(email, userRepository.findByEmailIgnoreCase(email).orElseThrow().getId());
    }

    private record TalentFixture(String email, Long id) {}
}