package com.growlink.backend.service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class MatchingText {
    private static final Set<String> STOP_WORDS = Set.of("a", "an", "and", "are", "for", "from", "in", "of", "on", "or", "the", "to", "with", "your", "you", "years", "year", "experience");

    private MatchingText() {}

    static Set<String> matchedSkills(Collection<String> desired, Collection<String> candidateSkills) {
        Set<String> matches = new LinkedHashSet<>();
        for (String candidate : candidateSkills) {
            if (desired.stream().anyMatch(value -> skillEquivalent(value, candidate))) matches.add(candidate);
        }
        return matches;
    }

    static Double skillScore(Collection<String> desired, Collection<String> candidateSkills) {
        if (desired.isEmpty() || candidateSkills.isEmpty()) return null;
        return (double) matchedSkills(desired, candidateSkills).size() / candidateSkills.size();
    }

    static Double textOverlap(String candidateText, Collection<String> sourceTerms) {
        if (candidateText == null || candidateText.isBlank() || sourceTerms.isEmpty()) return null;
        Set<String> source = tokens(String.join(" ", sourceTerms));
        Set<String> candidate = tokens(candidateText);
        if (source.isEmpty() || candidate.isEmpty()) return 0d;
        long overlap = source.stream().filter(candidate::contains).count();
        return (double) overlap / source.size();
    }

    static Double locationScore(String expected, String candidate) {
        if (expected == null || expected.isBlank() || candidate == null || candidate.isBlank()) return null;
        String left = normalize(expected);
        String right = normalize(candidate);
        return left.equals(right) || left.contains(right) || right.contains(left) ? 1d : 0d;
    }

    static boolean hasText(Collection<String> values) {
        return values != null && values.stream().anyMatch(value -> value != null && !value.isBlank());
    }

    static String normalize(String value) {
        String decomposed = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "");
        return decomposed.toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
    }

    static Set<String> tokens(String value) {
        if (value == null || value.isBlank()) return Set.of();
        return Arrays.stream(normalize(value).split("\\s+"))
                .filter(token -> token.length() > 1 && !STOP_WORDS.contains(token))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private static boolean skillEquivalent(String left, String right) {
        String normalizedLeft = normalize(left);
        String normalizedRight = normalize(right);
        if (normalizedLeft.isEmpty() || normalizedRight.isEmpty()) return false;
        if (normalizedLeft.equals(normalizedRight) || normalizedLeft.contains(normalizedRight) || normalizedRight.contains(normalizedLeft)) return true;
        Set<String> leftTokens = tokens(left);
        Set<String> rightTokens = tokens(right);
        if (leftTokens.isEmpty() || rightTokens.isEmpty()) return false;
        long overlap = leftTokens.stream().filter(rightTokens::contains).count();
        return overlap > 0 && (double) overlap / Math.min(leftTokens.size(), rightTokens.size()) >= 0.75;
    }
}