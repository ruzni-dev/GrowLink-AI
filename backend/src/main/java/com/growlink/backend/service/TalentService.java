package com.growlink.backend.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.growlink.backend.dto.EducationResponse;
import com.growlink.backend.dto.ExperienceResponse;
import com.growlink.backend.dto.InterestResponse;
import com.growlink.backend.dto.PublicSkillResponse;
import com.growlink.backend.dto.PublicTalentResponse;
import com.growlink.backend.dto.TalentCategoryResponse;
import com.growlink.backend.dto.TalentPageResponse;
import com.growlink.backend.entity.Education;
import com.growlink.backend.entity.TalentCategory;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserInterest;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserSkill;
import com.growlink.backend.entity.WorkExperience;
import com.growlink.backend.exception.InvalidProfileDataException;
import com.growlink.backend.exception.ResourceNotFoundException;
import com.growlink.backend.repository.EducationRepository;
import com.growlink.backend.repository.UserInterestRepository;
import com.growlink.backend.repository.UserProfileRepository;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.repository.UserSkillRepository;
import com.growlink.backend.repository.WorkExperienceRepository;

@Service
public class TalentService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;
    private final EducationRepository educationRepository;
    private final WorkExperienceRepository experienceRepository;
    private final UserInterestRepository userInterestRepository;

    public TalentService(UserRepository userRepository, UserProfileRepository profileRepository,
            UserSkillRepository userSkillRepository, EducationRepository educationRepository,
            WorkExperienceRepository experienceRepository, UserInterestRepository userInterestRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
        this.educationRepository = educationRepository;
        this.experienceRepository = experienceRepository;
        this.userInterestRepository = userInterestRepository;
    }

    @Transactional(readOnly = true)
    public TalentPageResponse search(String name, String skill, String categoryValue, String location,
            String proficiencyLevel, Integer minimumExperience, String education, int page, int size, String sortBy) {
        TalentCategory category = parseCategory(categoryValue);
        validateProficiency(proficiencyLevel);
        Pageable pageable = PageRequest.of(page, size, sort(sortBy));
        Page<User> users = userRepository.findAll(TalentSpecifications.matching(null, name, skill, category,
                location, proficiencyLevel, minimumExperience, education), pageable);
        Map<Long, PublicTalentResponse> profiles = mapProfiles(users.getContent());
        return TalentPageResponse.from(users.map(user -> profiles.get(user.getId())));
    }

    @Transactional(readOnly = true)
    public PublicTalentResponse getPublicProfile(Long id) {
        User user = userRepository.findOne(TalentSpecifications.matching(id, null, null, null, null, null, null, null))
                .orElseThrow(() -> new ResourceNotFoundException("Public talent profile was not found"));
        return mapProfiles(List.of(user)).get(user.getId());
    }

    public List<TalentCategoryResponse> getCategories() {
        return java.util.Arrays.stream(TalentCategory.values())
                .map(category -> new TalentCategoryResponse(category.name(), category.getLabel()))
                .toList();
    }

    private Map<Long, PublicTalentResponse> mapProfiles(List<User> users) {
        if (users.isEmpty()) return Map.of();
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, UserProfile> profiles = profileRepository.findByUser_IdIn(userIds).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));
        Map<Long, List<EducationResponse>> education = group(educationRepository.findByUser_IdInOrderByStartYearDesc(userIds), Education::getUser,
                row -> EducationResponse.from((Education) row));
        Map<Long, List<PublicSkillResponse>> skills = group(userSkillRepository.findByUser_IdInOrderBySkillNameAsc(userIds), UserSkill::getUser,
                row -> {
                    UserSkill skill = (UserSkill) row;
                    return new PublicSkillResponse(skill.getSkill().getName(), skill.getSkill().getCategory(),
                            skill.getProficiencyLevel().name(), skill.getYearsOfExperience(), skill.getDescription());
                });
        Map<Long, List<ExperienceResponse>> experience = group(experienceRepository.findByUser_IdInOrderByStartDateDesc(userIds), WorkExperience::getUser,
                row -> ExperienceResponse.from((WorkExperience) row));
        Map<Long, List<InterestResponse>> interests = group(userInterestRepository.findByUser_IdInOrderByInterestNameAsc(userIds), UserInterest::getUser,
                row -> InterestResponse.from(((UserInterest) row).getInterest()));

        return users.stream().collect(Collectors.toMap(User::getId, user -> {
            UserProfile profile = profiles.get(user.getId());
            return new PublicTalentResponse(user.getId(), user.getFullName(), profile.getProfileImage(), profile.getBio(),
                    firstNonBlank(profile.getLocation(), user.getLocation()), profile.getCountry(),
                    education.getOrDefault(user.getId(), List.of()), skills.getOrDefault(user.getId(), List.of()),
                    experience.getOrDefault(user.getId(), List.of()), interests.getOrDefault(user.getId(), List.of()));
        }));
    }

    private <T, R> Map<Long, List<R>> group(List<T> rows, Function<T, User> owner, Function<T, R> mapper) {
        return rows.stream().collect(Collectors.groupingBy(row -> owner.apply(row).getId(),
                Collectors.mapping(mapper, Collectors.toList())));
    }

    private TalentCategory parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return TalentCategory.fromValue(value);
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfileDataException(exception.getMessage());
        }
    }

    private void validateProficiency(String value) {
        if (value == null || value.isBlank()) return;
        try {
            com.growlink.backend.entity.ProficiencyLevel.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfileDataException("Unknown proficiency level");
        }
    }

    private Sort sort(String sortBy) {
        String field = sortBy == null || sortBy.isBlank() ? "name" : sortBy.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (field) {
            case "name" -> Sort.by(Sort.Order.asc("fullName"), Sort.Order.asc("id"));
            case "location" -> Sort.by(Sort.Order.asc("location"), Sort.Order.asc("fullName"));
            case "recent", "newest" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id"));
            default -> throw new InvalidProfileDataException("sortBy must be name, location, or recent");
        };
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }
}