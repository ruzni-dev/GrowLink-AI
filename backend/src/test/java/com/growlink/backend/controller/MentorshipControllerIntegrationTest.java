package com.growlink.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class MentorshipControllerIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;

    @Test
    void discoveryIsPublicButOfferManagementRequiresAuthentication() throws Exception {
        Account mentor = createAccount("PUBLIC");
        String title = "Public mentor " + UUID.randomUUID();
        createOffer(mentor, title);

        mockMvc.perform(get("/api/mentorship/offers").param("q", title))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value(title))
                .andExpect(jsonPath("$.content[0].mentorName").value(mentor.name()))
                .andExpect(jsonPath("$.content[0].mentorEmail").doesNotExist())
                .andExpect(jsonPath("$.content[0].phone").doesNotExist())
                .andExpect(jsonPath("$.content[0].password").doesNotExist());

        mockMvc.perform(get("/api/mentorship/my-offers"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/mentorship/offers").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateAndSuspendedMentorsAreExcludedFromDiscovery() throws Exception {
        Account privateMentor = createAccount("PRIVATE");
        String privateTitle = "Private offer " + UUID.randomUUID();
        long privateOfferId = createOffer(privateMentor, privateTitle);
        Account suspendedMentor = createAccount("PUBLIC");
        String suspendedTitle = "Suspended offer " + UUID.randomUUID();
        long suspendedOfferId = createOffer(suspendedMentor, suspendedTitle);
        User user = userRepository.findByEmailIgnoreCase(suspendedMentor.email()).orElseThrow();
        user.setStatus(UserStatus.SUSPENDED);
        userRepository.save(user);

        mockMvc.perform(get("/api/mentorship/offers").param("q", privateTitle))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(get("/api/mentorship/offers").param("q", suspendedTitle))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
        mockMvc.perform(get("/api/mentorship/offers/" + privateOfferId))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/mentorship/offers/" + suspendedOfferId))
                .andExpect(status().isNotFound());
    }

    @Test
    void menteeCanSendRequestButCannotDuplicateOrRequestSelf() throws Exception {
        Account mentor = createAccount("PUBLIC");
        Account mentee = createAccount("PUBLIC");
        long offerId = createOffer(mentor, "Learning offer " + UUID.randomUUID());

        mockMvc.perform(post("/api/mentorship/requests").header("Authorization", bearer(mentee.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(requestPayload(offerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.menteeName").value(mentee.name()))
                .andExpect(jsonPath("$.menteeEmail").doesNotExist());

        mockMvc.perform(post("/api/mentorship/requests").header("Authorization", bearer(mentee.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(requestPayload(offerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You already have a pending request for this offer"));

        mockMvc.perform(post("/api/mentorship/requests").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(requestPayload(offerId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You cannot request mentorship from yourself"));

        mockMvc.perform(get("/api/mentorship/requests/sent").header("Authorization", bearer(mentee.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
        mockMvc.perform(get("/api/mentorship/requests/sent").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void onlyOfferOwnerCanEditAndOnlyMentorCanAcceptOrReject() throws Exception {
        Account mentor = createAccount("PUBLIC");
        Account mentee = createAccount("PUBLIC");
        Account other = createAccount("PUBLIC");
        long offerId = createOffer(mentor, "Manage offer " + UUID.randomUUID());
        long requestId = createRequest(mentee, offerId);

        mockMvc.perform(put("/api/mentorship/offers/" + offerId).header("Authorization", bearer(other.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(offerPayload("Hijacked", mentor.skillId())))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/accept").header("Authorization", bearer(other.token())))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/accept").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ACCEPTED"));
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/reject").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pendingRequestCanBeRejectedOrCancelledByItsAllowedParticipant() throws Exception {
        Account mentor = createAccount("PUBLIC");
        Account mentee = createAccount("PUBLIC");
        Account anotherMentee = createAccount("PUBLIC");
        long offerId = createOffer(mentor, "Decisions offer " + UUID.randomUUID());
        long rejectedId = createRequest(mentee, offerId);
        long cancelledId = createRequest(anotherMentee, offerId);

        mockMvc.perform(patch("/api/mentorship/requests/" + rejectedId + "/reject").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REJECTED"));
        mockMvc.perform(patch("/api/mentorship/requests/" + cancelledId + "/cancel").header("Authorization", bearer(anotherMentee.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(patch("/api/mentorship/requests/" + rejectedId + "/cancel").header("Authorization", bearer(mentee.token())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptedMentorshipCanBeCompletedByParticipantAndCannotBeCompletedTwice() throws Exception {
        Account mentor = createAccount("PUBLIC");
        Account mentee = createAccount("PUBLIC");
        long offerId = createOffer(mentor, "Complete offer " + UUID.randomUUID());
        long requestId = createRequest(mentee, offerId);

        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/complete").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/accept").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/complete").header("Authorization", bearer(mentee.token())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mockMvc.perform(patch("/api/mentorship/requests/" + requestId + "/complete").header("Authorization", bearer(mentor.token())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pausedOrClosedOffersCannotReceiveRequestsAndOfferTransitionsAreValidated() throws Exception {
        Account mentor = createAccount("PUBLIC");
        Account mentee = createAccount("PUBLIC");
        long offerId = createOffer(mentor, "Status offer " + UUID.randomUUID());

        mockMvc.perform(patch("/api/mentorship/offers/" + offerId + "/status").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"PAUSED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PAUSED"));
        mockMvc.perform(post("/api/mentorship/requests").header("Authorization", bearer(mentee.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(requestPayload(offerId)))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/api/mentorship/offers/" + offerId + "/status").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/mentorship/offers/" + offerId + "/status").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"CLOSED\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CLOSED"));
        mockMvc.perform(patch("/api/mentorship/offers/" + offerId + "/status").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/mentorship/my-offers").header("Authorization", bearer(mentor.token())).param("status", "CLOSED"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    private Account createAccount(String visibility) throws Exception {
        String suffix = UUID.randomUUID().toString();
        String name = "Member " + suffix.substring(0, 8);
        String email = "member-" + suffix + "@example.com";
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"" + name + "\",\"email\":\"" + email
                                + "\",\"password\":\"StrongPass1\",\"location\":\"Colombo\",\"role\":\"USER\"}"))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"StrongPass1\"}"))
                .andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(login.getResponse().getContentAsString()).get("accessToken").asText();
        String auth = bearer(token);
        mockMvc.perform(put("/api/profiles/me").header("Authorization", auth).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bio\":\"Community member with practical mentoring experience.\",\"location\":\"Colombo\",\"visibility\":\"" + visibility + "\"}"))
                .andExpect(status().isOk());
        MvcResult skill = mockMvc.perform(post("/api/profiles/me/skills").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Mentorship Skill " + suffix + "\",\"category\":\"MENTORSHIP_GUIDANCE\",\"proficiencyLevel\":\"ADVANCED\",\"yearsOfExperience\":6}"))
                .andExpect(status().isCreated()).andReturn();
        long skillId = objectMapper.readTree(skill.getResponse().getContentAsString()).get("skillId").asLong();
        long userId = userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        return new Account(email, token, name, skillId, userId);
    }

    private long createOffer(Account mentor, String title) throws Exception {
        MvcResult response = mockMvc.perform(post("/api/mentorship/offers").header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerPayload(title, mentor.skillId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asLong();
    }

    private String offerPayload(String title, long skillId) {
        return "{\"title\":\"" + title + "\",\"description\":\"Guidance and practical sessions for people building community technology skills.\",\"category\":\"MENTORSHIP_GUIDANCE\",\"skillIds\":[" + skillId + "],\"experience\":\"Six years of experience\",\"availability\":\"Two evenings each month\",\"preferredMode\":\"BOTH\",\"location\":\"Colombo\"}";
    }

    private long createRequest(Account mentee, long offerId) throws Exception {
        MvcResult response = mockMvc.perform(post("/api/mentorship/requests").header("Authorization", bearer(mentee.token()))
                        .contentType(MediaType.APPLICATION_JSON).content(requestPayload(offerId)))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(response.getResponse().getContentAsString()).get("id").asLong();
    }

    private String requestPayload(long offerId) {
        return "{\"offerId\":" + offerId + ",\"message\":\"I would value your guidance as I build my first community learning project.\",\"goals\":\"I want to learn planning and delivery.\",\"preferredMode\":\"ONLINE\"}";
    }

    private String bearer(String token) { return "Bearer " + token; }
    private record Account(String email, String token, String name, long skillId, long userId) {}
}