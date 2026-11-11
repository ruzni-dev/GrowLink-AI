package com.growlink.backend.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.growlink.backend.dto.MentorRecommendationResponse;
import com.growlink.backend.dto.OpportunityRecommendationResponse;
import com.growlink.backend.dto.RecommendationDashboardResponse;
import com.growlink.backend.dto.RecommendationMatchResponse;
import com.growlink.backend.dto.RecommendationPreferencesRequest;
import com.growlink.backend.dto.RecommendationPreferencesResponse;
import com.growlink.backend.dto.TalentRecommendationResponse;
import com.growlink.backend.dto.LearningRecommendationResponse;
import com.growlink.backend.service.RecommendationService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/dashboard")
    public RecommendationDashboardResponse dashboard(Authentication authentication) {
        return recommendationService.dashboard(authentication.getName());
    }

    @GetMapping("/opportunities")
    public List<OpportunityRecommendationResponse> opportunities(Authentication authentication,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int limit) {
        return recommendationService.opportunities(authentication.getName(), limit);
    }

    @GetMapping("/mentors")
    public List<MentorRecommendationResponse> mentors(Authentication authentication,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int limit) {
        return recommendationService.mentors(authentication.getName(), limit);
    }

    @GetMapping("/talents")
    public List<TalentRecommendationResponse> talents(Authentication authentication,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int limit) {
        return recommendationService.talents(authentication.getName(), limit);
    }

    @GetMapping("/learning")
    public List<LearningRecommendationResponse> learning(Authentication authentication,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int limit) {
        return recommendationService.learning(authentication.getName(), limit);
    }

    @GetMapping("/match/opportunity/{id}")
    public RecommendationMatchResponse matchOpportunity(Authentication authentication, @PathVariable @Min(1) Long id) {
        return recommendationService.matchOpportunity(authentication.getName(), id);
    }

    @GetMapping("/match/mentor/{id}")
    public RecommendationMatchResponse matchMentor(Authentication authentication, @PathVariable @Min(1) Long id) {
        return recommendationService.matchMentor(authentication.getName(), id);
    }

    @GetMapping("/match/talent/{id}")
    public RecommendationMatchResponse matchTalent(Authentication authentication, @PathVariable @Min(1) Long id) {
        return recommendationService.matchTalent(authentication.getName(), id);
    }

    @GetMapping("/preferences")
    public RecommendationPreferencesResponse preferences(Authentication authentication) {
        return recommendationService.getPreferences(authentication.getName());
    }

    @PutMapping("/preferences")
    public RecommendationPreferencesResponse updatePreferences(Authentication authentication,
            @Valid @RequestBody RecommendationPreferencesRequest request) {
        return recommendationService.updatePreferences(authentication.getName(), request);
    }
}