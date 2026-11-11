package com.growlink.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
class OpportunityControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void opportunityCanBeCreatedUpdatedAndPublishedByItsCreator() throws Exception {
        Account creator = createAccount();
        String title = "Published opportunity " + UUID.randomUUID();
        long id = createOpportunity(creator, title, "DRAFT");

        mockMvc.perform(get("/api/opportunities/search").param("q", title))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(put("/api/opportunities/" + id).header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(opportunityPayload("Updated " + title, "DRAFT")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated " + title));
        mockMvc.perform(patch("/api/opportunities/" + id + "/status").header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PUBLISHED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));
        mockMvc.perform(get("/api/opportunities/" + id))
                .andExpect(status().isOk()).andExpect(jsonPath("$.title").value("Updated " + title));
    }

    @Test
    void publicSearchFiltersAndHidesCreatorContactInformation() throws Exception {
        Account creator = createAccount();
        String title = "Filtered role " + UUID.randomUUID();
        long id = createOpportunity(creator, title, "PUBLISHED");

        mockMvc.perform(get("/api/opportunities/search")
                        .param("q", title)
                        .param("category", "JOB")
                        .param("location", "Colombo")
                        .param("remote", "true")
                        .param("organization", "GrowLink")
                        .param("deadlineAfter", LocalDate.now().plusDays(2).toString())
                        .param("deadlineBefore", LocalDate.now().plusDays(60).toString())
                        .param("page", "0").param("size", "1").param("sortBy", "deadline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(id))
                .andExpect(jsonPath("$.content[0].remote").value(true))
                .andExpect(jsonPath("$.content[0].createdBy").doesNotExist())
                .andExpect(jsonPath("$.content[0].email").doesNotExist());

        mockMvc.perform(get("/api/opportunities/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInformation").value((Object) null));
        mockMvc.perform(get("/api/opportunities/" + id).header("Authorization", bearer(creator.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInformation").value("talent@example.org"));
    }

    @Test
    void bookmarkIsUniqueAndCanBeRemoved() throws Exception {
        Account creator = createAccount();
        Account member = createAccount();
        long id = createOpportunity(creator, "Save me " + UUID.randomUUID(), "PUBLISHED");

        mockMvc.perform(post("/api/opportunities/" + id + "/save").header("Authorization", bearer(member.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.opportunity.id").value(id));
        mockMvc.perform(post("/api/opportunities/" + id + "/save").header("Authorization", bearer(member.token())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/opportunities/saved").header("Authorization", bearer(member.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
        mockMvc.perform(delete("/api/opportunities/" + id + "/save").header("Authorization", bearer(member.token())))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/opportunities/saved").header("Authorization", bearer(member.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void applicationTrackingIsUniqueAndOnlyOwnerCanUpdateIt() throws Exception {
        Account creator = createAccount();
        Account member = createAccount();
        Account other = createAccount();
        long id = createOpportunity(creator, "Tracker " + UUID.randomUUID(), "PUBLISHED");

        mockMvc.perform(post("/api/opportunities/" + id + "/track").header("Authorization", bearer(member.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"notes\":\"Review requirements\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("INTERESTED"));
        mockMvc.perform(post("/api/opportunities/" + id + "/track").header("Authorization", bearer(member.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        MvcResult applicationList = mockMvc.perform(get("/api/opportunities/applications").header("Authorization", bearer(member.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1))).andReturn();
        JsonNode application = objectMapper.readTree(applicationList.getResponse().getContentAsString()).get("content").get(0);
        long applicationId = application.get("id").asLong();

        mockMvc.perform(patch("/api/opportunities/applications/" + applicationId + "/status")
                        .header("Authorization", bearer(other.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/opportunities/applications/" + applicationId + "/status")
                        .header("Authorization", bearer(member.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPLIED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.appliedAt").isNotEmpty());
        mockMvc.perform(patch("/api/opportunities/applications/" + applicationId + "/notes")
                        .header("Authorization", bearer(member.token())).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"notes\":\"Submitted application through external site\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.notes").value("Submitted application through external site"));
    }

    @Test
    void ownersOnlyCanManageListingsAndClosedExpiredOrArchivedListingsCannotBeTracked() throws Exception {
        Account creator = createAccount();
        Account other = createAccount();
        long id = createOpportunity(creator, "Owner only " + UUID.randomUUID(), "PUBLISHED");

        mockMvc.perform(put("/api/opportunities/" + id).header("Authorization", bearer(other.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(opportunityPayload("Stolen title", "PUBLISHED")))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/opportunities/" + id + "/status").header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/opportunities/" + id + "/track").header("Authorization", bearer(other.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/opportunities/" + id))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/opportunities/my-opportunities"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/opportunities/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidApplicationUrlAndPastPublishedDeadlineAreRejected() throws Exception {
        Account creator = createAccount();
        mockMvc.perform(post("/api/opportunities").header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(opportunityPayload("Invalid URL", "DRAFT").replace("https://apply.example.org/growlink", "javascript:alert(1)")))
                .andExpect(status().isBadRequest());

        String expired = opportunityPayload("Expired " + UUID.randomUUID(), "PUBLISHED")
                .replace(LocalDate.now().plusDays(14).toString(), LocalDate.now().minusDays(1).toString());
        mockMvc.perform(post("/api/opportunities").header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(expired))
                .andExpect(status().isBadRequest());
    }

    private long createOpportunity(Account creator, String title, String status) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/opportunities").header("Authorization", bearer(creator.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(opportunityPayload(title, status)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(status))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String opportunityPayload(String title, String status) {
        return "{\"title\":\"" + title + "\",\"description\":\"A community opportunity for people growing practical career and education skills.\",\"organizationName\":\"GrowLink Community\",\"category\":\"JOB\",\"opportunityType\":\"Full-time\",\"location\":\"Colombo\",\"remote\":true,\"eligibility\":\"Open to community members\",\"requiredSkills\":[\"Java\",\"Communication\"],\"qualification\":\"Relevant experience\",\"applicationUrl\":\"https://apply.example.org/growlink\",\"contactInformation\":\"talent@example.org\",\"startDate\":\"" + LocalDate.now().plusDays(7) + "\",\"deadline\":\"" + LocalDate.now().plusDays(14) + "\",\"status\":\"" + status + "\"}";
    }

    private Account createAccount() throws Exception {
        String email = "opportunity-" + UUID.randomUUID() + "@example.com";
        String name = "Opportunity Member";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"StrongPass1\",\"location\":\"Colombo\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"StrongPass1\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        return new Account(email, token);
    }

    private String bearer(String token) { return "Bearer " + token; }
    private record Account(String email, String token) {}
}