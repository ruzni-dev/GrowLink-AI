package com.growlink.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.growlink.backend.entity.Opportunity;

@Component
public class SkillGapAnalysisStrategy {
    public List<SkillGap> analyze(RecommendationContext context, List<Opportunity> opportunities) {
        Map<String, SkillDemand> demand = new LinkedHashMap<>();
        for (Opportunity opportunity : opportunities) {
            for (String skill : opportunity.getRequiredSkills()) {
                RecommendationContext.SkillFact known = context.skills().values().stream()
                    .filter(current -> MatchingText.matchedSkills(Set.of(current.name()), Set.of(skill)).contains(skill))
                    .findFirst().orElse(null);
                boolean underdeveloped = known != null && ("BEGINNER".equals(known.proficiencyLevel())
                    || "INTERMEDIATE".equals(known.proficiencyLevel()));
                if (known != null && !underdeveloped) continue;
                String key = MatchingText.normalize(skill);
                if (key.isEmpty()) continue;
                SkillDemand existing = demand.get(key);
                demand.put(key, existing == null ? new SkillDemand(skill, 1, underdeveloped)
                    : new SkillDemand(existing.name(), existing.count() + 1, existing.underdeveloped() || underdeveloped));
            }
        }
        List<SkillDemand> ordered = demand.values().stream()
                .sorted(Comparator.comparingInt(SkillDemand::count).reversed().thenComparing(SkillDemand::name, String.CASE_INSENSITIVE_ORDER))
                .toList();
        int maxDemand = ordered.stream().mapToInt(SkillDemand::count).max().orElse(1);
        List<SkillGap> results = new ArrayList<>();
        for (SkillDemand item : ordered) {
            Double interestScore = MatchingText.textOverlap(item.name(), context.interests());
            Double goalScore = MatchingText.textOverlap(item.name(), context.learningGoals());
                Map<MatchingDimension, String> evidence = new java.util.EnumMap<>(MatchingDimension.class);
                if (item.underdeveloped()) evidence.put(MatchingDimension.EXPERIENCE, "You have a beginner or intermediate level in this skill and can build it further.");
                MatchingAnalysis analysis = new MatchingAnalysis(
                    Map.of(MatchingDimension.SKILL, (double) item.count() / maxDemand), Set.of(), evidence);
            results.add(new SkillGap(item.name(), item.count(), interestScore, goalScore, analysis));
        }
        return results;
    }

    private record SkillDemand(String name, int count, boolean underdeveloped) {}

    public record SkillGap(String name, int demandCount, Double interestScore, Double goalScore, MatchingAnalysis analysis) {}
}