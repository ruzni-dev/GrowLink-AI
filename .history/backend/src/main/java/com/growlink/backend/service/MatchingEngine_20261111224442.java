package com.growlink.backend.service;

import org.springframework.stereotype.Service;

import com.growlink.backend.entity.MentorshipOffer;
import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.dto.PublicTalentResponse;

@Service
public class MatchingEngine {
    private final OpportunityMatchingStrategy opportunityStrategy;
    private final MentorMatchingStrategy mentorStrategy;
    private final TalentMatchingStrategy talentStrategy;
    private final MatchingScoreCalculator scoreCalculator;
    private final RecommendationExplanationService explanationService;

    public MatchingEngine(OpportunityMatchingStrategy opportunityStrategy, MentorMatchingStrategy mentorStrategy,
            TalentMatchingStrategy talentStrategy, MatchingScoreCalculator scoreCalculator,
            RecommendationExplanationService explanationService) {
        this.opportunityStrategy = opportunityStrategy;
        this.mentorStrategy = mentorStrategy;
        this.talentStrategy = talentStrategy;
        this.scoreCalculator = scoreCalculator;
        this.explanationService = explanationService;
    }

    public MatchingResult opportunity(RecommendationContext context, Opportunity opportunity) {
        return explanationService.explain(opportunityStrategy.analyze(context, opportunity), scoreCalculator);
    }

    public MatchingResult mentor(RecommendationContext context, MentorshipOffer offer) {
        return explanationService.explain(mentorStrategy.analyze(context, offer), scoreCalculator);
    }

    public MatchingResult talent(RecommendationContext context, PublicTalentResponse talent) {
        return explanationService.explain(talentStrategy.analyze(context, talent), scoreCalculator);
    }

    public MatchingResult analyze(MatchingAnalysis analysis) {
        return explanationService.explain(analysis, scoreCalculator);
    }

    public MatchingScoreCalculator scoreCalculator() {
        return scoreCalculator;
    }

    public RecommendationExplanationService explanationService() {
        return explanationService;
    }
}