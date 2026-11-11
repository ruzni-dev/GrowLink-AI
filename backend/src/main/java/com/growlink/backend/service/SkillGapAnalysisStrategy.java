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
                if (context.currentSkills().stream().anyMatch(current -> MatchingText.matchedSkills(Set.of(current), Set.of(skill)).contains(skill))) {
                    continue;
                }
                String key = MatchingText.normalize(skill);
                if (key.isEmpty()) continue;
                SkillDemand existing = demand.get(key);
                demand.put(key, existing == null ? new SkillDemand(skill, 1) : new SkillDemand(existing.name(), existing.count() + 1));
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
            MatchingAnalysis analysis = new MatchingAnalysis(
                    Map.of(MatchingDimension.SKILL, (double) item.count() / maxDemand), Set.of(), Map.of());
            results.add(new SkillGap(item.name(), item.count(), interestScore, goalScore, analysis));
        }
        return results;
    }

    private record SkillDemand(String name, int count) {}

    public record SkillGap(String name, int demandCount, Double interestScore, Double goalScore, MatchingAnalysis analysis) {}
}