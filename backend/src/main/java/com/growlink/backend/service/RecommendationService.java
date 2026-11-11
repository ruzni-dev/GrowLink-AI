package com.growlink.backend.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.growlink.backend.dto.MentorRecommendationResponse;
import com.growlink.backend.dto.OpportunityApplicationResponse;
import com.growlink.backend.dto.OpportunityRecommendationResponse;
import com.growlink.backend.dto.OpportunityResponse;
import com.growlink.backend.dto.PublicTalentResponse;
import com.growlink.backend.dto.RecommendationDashboardResponse;
import com.growlink.backend.dto.RecommendationMatchResponse;
import com.growlink.backend.dto.RecommendationPreferencesRequest;
import com.growlink.backend.dto.RecommendationPreferencesResponse;
import com.growlink.backend.dto.TalentRecommendationResponse;
import com.growlink.backend.dto.LearningRecommendationResponse;
import com.growlink.backend.entity.Education;
import com.growlink.backend.entity.Interest;
import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.OpportunityStatus;
import com.growlink.backend.entity.RecommendationPreference;
import com.growlink.backend.entity.RemotePreference;
import com.growlink.backend.entity.TalentCategory;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserInterest;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserSkill;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.entity.WorkExperience;
import com.growlink.backend.exception.InvalidProfileDataException;
import com.growlink.backend.exception.ResourceNotFoundException;
import com.growlink.backend.repository.EducationRepository;
import com.growlink.backend.repository.MentorshipOfferRepository;
import com.growlink.backend.repository.OpportunityRepository;
import com.growlink.backend.repository.RecommendationPreferenceRepository;
import com.growlink.backend.repository.UserInterestRepository;
import com.growlink.backend.repository.UserProfileRepository;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.repository.UserSkillRepository;
import com.growlink.backend.repository.WorkExperienceRepository;
import com.growlink.backend.dto.MentorshipOfferResponse;

@Service
public class RecommendationService {
    private static final int CANDIDATE_LIMIT = 100;
    private static final int RESULT_LIMIT = 20;

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository experienceRepository;
    private final UserInterestRepository userInterestRepository;
    private final OpportunityRepository opportunityRepository;
    private final RecommendationPreferenceRepository preferenceRepository;
    private final TalentService talentService;
    private final MentorshipService mentorshipService;
    private final OpportunityMatchingStrategy opportunityStrategy;
    private final MentorMatchingStrategy mentorStrategy;
    private final TalentMatchingStrategy talentStrategy;
    private final SkillGapAnalysisStrategy skillGapStrategy;
    private final MatchingEngine matchingEngine;

    public RecommendationService(UserRepository userRepository, UserProfileRepository profileRepository,
            UserSkillRepository userSkillRepository, EducationRepository educationRepository,
            WorkExperienceRepository experienceRepository, UserInterestRepository userInterestRepository,
            OpportunityRepository opportunityRepository, RecommendationPreferenceRepository preferenceRepository,
            TalentService talentService, MentorshipService mentorshipService,
            OpportunityMatchingStrategy opportunityStrategy, MentorMatchingStrategy mentorStrategy,
            TalentMatchingStrategy talentStrategy, SkillGapAnalysisStrategy skillGapStrategy,
            MatchingEngine matchingEngine) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.userInterestRepository = userInterestRepository;
        this.opportunityRepository = opportunityRepository;
        this.preferenceRepository = preferenceRepository;
        this.talentService = talentService;
        this.mentorshipService = mentorshipService;
        this.opportunityStrategy = opportunityStrategy;
        this.mentorStrategy = mentorStrategy;
        this.talentStrategy = talentStrategy;
        this.skillGapStrategy = skillGapStrategy;
        this.matchingEngine = matchingEngine;
    }

    @Transactional(readOnly = true)
    public RecommendationDashboardResponse dashboard(String email) {
        RecommendationContext context = context(email);
        List<OpportunityRecommendationResponse> opportunities = opportunityRecommendations(context, 4);
        List<MentorRecommendationResponse> mentors = mentorRecommendations(context, 4);
        List<TalentRecommendationResponse> talents = talentRecommendations(context, 4);
        List<LearningRecommendationResponse> learning = learningRecommendations(context, 5);
        return new RecommendationDashboardResponse(context.user().getFullName(), opportunities, mentors, talents, learning);
    }

    @Transactional(readOnly = true)
    public List<OpportunityRecommendationResponse> opportunities(String email, int limit) {
        return opportunityRecommendations(context(email), limit);
    }

    @Transactional(readOnly = true)
    public List<MentorRecommendationResponse> mentors(String email, int limit) {
        return mentorRecommendations(context(email), limit);
    }

    @Transactional(readOnly = true)
    public List<TalentRecommendationResponse> talents(String email, int limit) {
        return talentRecommendations(context(email), limit);
    }

    @Transactional(readOnly = true)
    public List<LearningRecommendationResponse> learning(String email, int limit) {
        return learningRecommendations(context(email), limit);
    }

    @Transactional(readOnly = true)
    public RecommendationMatchResponse matchOpportunity(String email, Long id) {
        RecommendationContext context = context(email);
        Opportunity opportunity = findOpportunity(id, context.user().getId());
        MatchingResult result = matchingEngine.analyze(opportunityStrategy.analyze(context, opportunity));
        return new RecommendationMatchResponse("OPPORTUNITY", id, result.percentage(),
                List.copyOf(result.matchedSkills()), result.reasons());
    }

    @Transactional(readOnly = true)
    public RecommendationMatchResponse matchMentor(String email, Long mentorId) {
        RecommendationContext context = context(email);
        if (context.user().getId().equals(mentorId)) throw missing("Mentor");
        List<MentorshipOfferResponse> offers = mentorshipService.publicOffersForMentor(mentorId);
        Ranked<MentorRecommendationResponse> best = offers.stream()
                .map(offer -> mentorResult(context, offer))
                .max(Comparator.comparingInt(item -> item.response().matchPercentage()))
                .orElseThrow(() -> missing("Mentor"));
        return new RecommendationMatchResponse("MENTOR", mentorId, best.response().matchPercentage(),
                best.response().matchedSkills(), best.response().reasons());
    }

    @Transactional(readOnly = true)
    public RecommendationMatchResponse matchTalent(String email, Long talentId) {
        RecommendationContext context = context(email);
        if (context.user().getId().equals(talentId)) throw missing("Talent");
        PublicTalentResponse talent = talentService.getPublicProfile(talentId);
        MatchingResult result = matchingEngine.analyze(talentStrategy.analyze(context, talent));
        return new RecommendationMatchResponse("TALENT", talentId, result.percentage(),
                List.copyOf(result.matchedSkills()), result.reasons());
    }

    @Transactional
    public RecommendationPreferencesResponse getPreferences(String email) {
        return preferenceResponse(findOrCreatePreference(activeUser(email)));
    }

    @Transactional
    public RecommendationPreferencesResponse updatePreferences(String email, RecommendationPreferencesRequest request) {
        User user = activeUser(email);
        validateCategories(request.preferredCategories());
        RecommendationPreference preference = findOrCreatePreference(user);
        preference.setPreferredCategories(normalize(request.preferredCategories()));
        preference.setPreferredLocations(normalize(request.preferredLocations()));
        preference.setInterestedSkills(normalize(request.interestedSkills()));
        preference.setLearningInterests(normalize(request.learningInterests()));
        preference.setLearningGoals(normalize(request.learningGoals()));
        preference.setRemotePreference(request.remotePreference());
        return preferenceResponse(preferenceRepository.save(preference));
    }

    private List<OpportunityRecommendationResponse> opportunityRecommendations(RecommendationContext context, int limit) {
        List<Opportunity> candidates = candidateOpportunities(context.user().getId());
        Map<Long, Opportunity> withSkills = opportunityRepository.findWithRequiredSkillsByIdIn(
                        candidates.stream().map(Opportunity::getId).toList()).stream()
                .collect(Collectors.toMap(Opportunity::getId, item -> item));
        return candidates.stream().map(candidate -> {
                    Opportunity loaded = withSkills.get(candidate.getId());
                    MatchingResult match = matchingEngine.opportunity(context, loaded);
                    return new Ranked<>(new OpportunityRecommendationResponse(opportunityResponse(loaded),
                            match.percentage(), List.copyOf(match.matchedSkills()), match.reasons()), match.percentage(), loaded.getId());
                }).sorted(rankedOrder()).limit(limit).map(Ranked::response).toList();
    }

    private List<MentorRecommendationResponse> mentorRecommendations(RecommendationContext context, int limit) {
        return mentorshipService.findOffers(null, null, null, 0, CANDIDATE_LIMIT, "recent", context.user().getId())
                .content().stream().map(offer -> mentorResult(context, offer)).sorted(rankedOrder())
                .limit(limit).map(Ranked::response).toList();
    }

    private Ranked<MentorRecommendationResponse> mentorResult(RecommendationContext context, MentorshipOfferResponse offer) {
        MatchingResult match = matchingEngine.analyze(mentorStrategy.analyze(context, offer));
        return new Ranked<>(new MentorRecommendationResponse(offer.id(), offer.mentorName(), offer.mentorProfileImage(),
                offer.mentorLocation(), offer.title(), offer.description(), offer.category(),
                offer.skills().stream().map(skill -> skill.name()).toList(), offer.experience(), offer.availability(),
                offer.preferredMode(), match.percentage(), List.copyOf(match.matchedSkills()), match.reasons()),
                match.percentage(), offer.id());
    }

    private List<TalentRecommendationResponse> talentRecommendations(RecommendationContext context, int limit) {
        List<PublicTalentResponse> candidates = talentService.search(null, null, null, null, null, null, null,
                0, CANDIDATE_LIMIT, "recent").content().stream()
                .filter(talent -> !context.user().getId().equals(talent.id())).toList();
        return candidates.stream().map(talent -> {
                    MatchingResult match = matchingEngine.talent(context, talent);
                    return new Ranked<>(new TalentRecommendationResponse(talent, match.percentage(),
                            List.copyOf(match.matchedSkills()), match.reasons()), match.percentage(), talent.id());
                }).sorted(rankedOrder()).limit(limit).map(Ranked::response).toList();
    }

    private List<LearningRecommendationResponse> learningRecommendations(RecommendationContext context, int limit) {
        List<Opportunity> candidates = candidateOpportunities(context.user().getId());
        if (candidates.isEmpty()) return List.of();
        List<Opportunity> loaded = opportunityRepository.findWithRequiredSkillsByIdIn(
                candidates.stream().map(Opportunity::getId).toList());
        List<SkillGapAnalysisStrategy.SkillGap> gaps = skillGapStrategy.analyze(context, loaded);
        return gaps.stream().map(gap -> {
                    MatchingAnalysis analysis = gap.analysis();
                    Double interest = MatchingText.textOverlap(gap.name(), context.interests());
                    Double goal = MatchingText.textOverlap(gap.name(), context.learningGoals());
                    Map<MatchingDimension, Double> scores = new java.util.EnumMap<>(analysis.scores());
                    Map<MatchingDimension, String> evidence = new java.util.EnumMap<>(analysis.evidence());
                    if (interest != null) {
                        scores.put(MatchingDimension.INTEREST, interest);
                        if (interest > 0) evidence.put(MatchingDimension.INTEREST, "This skill relates to an interest you selected.");
                    }
                    if (goal != null) {
                        scores.put(MatchingDimension.GOALS, goal);
                        if (goal > 0) evidence.put(MatchingDimension.GOALS, "This skill supports a learning goal you added.");
                    }
                    MatchingResult result = matchingEngine.analyze(new MatchingAnalysis(scores, Set.of(), evidence));
                    return new Ranked<>(new LearningRecommendationResponse(gap.name(), result.percentage(),
                            gap.demandCount(), result.reasons()), result.percentage(), (long) gap.name().hashCode());
                }).sorted(rankedOrder()).limit(limit).map(Ranked::response).toList();
    }

    private List<Opportunity> candidateOpportunities(Long currentUserId) {
        var criteria = OpportunitySpecifications.discover(null, null, null, null, null, null, null)
                .and(OpportunitySpecifications.excludingCreator(currentUserId));
        Page<Opportunity> page = opportunityRepository.findAll(criteria,
                PageRequest.of(0, CANDIDATE_LIMIT, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return page.getContent();
    }

    private Opportunity findOpportunity(Long id, Long currentUserId) {
        return opportunityRepository.findOne(OpportunitySpecifications.discover(null, null, null, null, null, null, null)
                        .and(OpportunitySpecifications.excludingCreator(currentUserId))
                        .and((root, query, builder) -> builder.equal(root.get("id"), id)))
                .orElseThrow(() -> missing("Opportunity"));
    }

    private RecommendationContext context(String email) {
        User user = activeUser(email);
        UserProfile profile = profileRepository.findByUserEmailIgnoreCase(email).orElse(null);
        RecommendationPreference preference = preferenceRepository.findByUserEmailIgnoreCase(email)
                .orElseGet(() -> new RecommendationPreference());
        List<UserSkill> userSkills = userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(email);
        Map<String, RecommendationContext.SkillFact> skills = userSkills.stream().collect(Collectors.toMap(
                item -> MatchingText.normalize(item.getSkill().getName()),
                item -> new RecommendationContext.SkillFact(item.getSkill().getName(), item.getSkill().getCategory(),
                        item.getProficiencyLevel().name(), item.getYearsOfExperience()),
                (left, right) -> left, LinkedHashMap::new));

        List<String> education = educationRepository.findByUserEmailIgnoreCaseOrderByStartYearDesc(email).stream()
                .map(item -> join(item.getQualification(), item.getFieldOfStudy(), item.getInstitutionName(), item.getDescription()))
                .filter(value -> !value.isBlank()).toList();
        List<String> experience = experienceRepository.findByUserEmailIgnoreCaseOrderByStartDateDesc(email).stream()
                .map(item -> join(item.getJobTitle(), item.getOrganization(), item.getDescription()))
                .filter(value -> !value.isBlank()).toList();
        Set<String> interests = userInterestRepository.findByUserEmailIgnoreCaseOrderByInterestNameAsc(email).stream()
                .map(UserInterest::getInterest).map(Interest::getName).collect(Collectors.toCollection(LinkedHashSet::new));
        interests.addAll(preference.getLearningInterests());
        String location = profile != null && profile.getLocation() != null && !profile.getLocation().isBlank()
                ? profile.getLocation() : user.getLocation();
        return new RecommendationContext(user, location, skills, education, experience,
                normalize(preference.getPreferredCategories()), normalize(preference.getPreferredLocations()),
                normalize(preference.getInterestedSkills()), normalize(interests), normalize(preference.getLearningGoals()),
                preference.getRemotePreference() == null ? RemotePreference.ANY : preference.getRemotePreference());
    }

    private RecommendationPreference findOrCreatePreference(User user) {
        return preferenceRepository.findByUserEmailIgnoreCase(user.getEmail()).orElseGet(() -> {
            RecommendationPreference preference = new RecommendationPreference();
            preference.setUser(user);
            return preferenceRepository.save(preference);
        });
    }

    private RecommendationPreferencesResponse preferenceResponse(RecommendationPreference preference) {
        return new RecommendationPreferencesResponse(Set.copyOf(preference.getPreferredCategories()),
                Set.copyOf(preference.getPreferredLocations()), Set.copyOf(preference.getInterestedSkills()),
                Set.copyOf(preference.getLearningInterests()), Set.copyOf(preference.getLearningGoals()),
                preference.getRemotePreference(), preference.getUpdatedAt());
    }

    private void validateCategories(Set<String> values) {
        for (String value : values == null ? Set.<String>of() : values) {
            String normalized = value.trim().toUpperCase(Locale.ROOT);
            boolean opportunityCategory = Arrays.stream(OpportunityCategory.values()).anyMatch(category -> category.name().equals(normalized));
            boolean talentCategory = Arrays.stream(TalentCategory.values()).anyMatch(category -> category.name().equals(normalized)
                    || category.getLabel().equalsIgnoreCase(value.trim()));
            if (!opportunityCategory && !talentCategory) throw new InvalidProfileDataException("Unknown recommendation category: " + value);
        }
    }

    private Set<String> normalize(Set<String> values) {
        if (values == null) return Set.of();
        return values.stream().filter(value -> value != null && !value.isBlank()).map(String::trim)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String join(String... values) {
        return Arrays.stream(values).filter(value -> value != null && !value.isBlank()).reduce((a, b) -> a + " " + b).orElse("");
    }

    private User activeUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> missing("User"));
        if (user.getStatus() != UserStatus.ACTIVE) throw missing("User");
        return user;
    }

    private OpportunityResponse opportunityResponse(Opportunity opportunity) {
        return new OpportunityResponse(opportunity.getId(), opportunity.getTitle(), opportunity.getDescription(),
                opportunity.getOrganizationName(), opportunity.getCategory(), opportunity.getOpportunityType(),
                opportunity.getLocation(), opportunity.isRemote(), opportunity.getEligibility(),
                Set.copyOf(opportunity.getRequiredSkills()), opportunity.getQualification(), opportunity.getApplicationUrl(),
                null, opportunity.getStartDate(), opportunity.getDeadline(), opportunity.getStatus(),
                opportunity.getCreatedAt(), opportunity.getUpdatedAt());
    }

    private java.util.function.ToIntFunction<Ranked<?>> orderByScore() {
        return item -> item.score();
    }

    private <T> Comparator<Ranked<T>> rankedOrder() {
        return Comparator.<Ranked<T>>comparingInt(orderByScore()).reversed().thenComparingLong(Ranked::tieBreaker);
    }

    private String clean(String value) { return value == null || value.isBlank() ? "" : value.trim(); }
    private ResourceNotFoundException missing(String resource) { return new ResourceNotFoundException(resource + " was not found"); }

    private record Ranked<T>(T response, int score, Long tieBreaker) {}
}