package com.growlink.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.growlink.backend.dto.EducationRequest;
import com.growlink.backend.dto.EducationResponse;
import com.growlink.backend.dto.ExperienceRequest;
import com.growlink.backend.dto.ExperienceResponse;
import com.growlink.backend.dto.InterestResponse;
import com.growlink.backend.dto.ProfileRequest;
import com.growlink.backend.dto.ProfileResponse;
import com.growlink.backend.dto.SkillRequest;
import com.growlink.backend.dto.SkillResponse;
import com.growlink.backend.entity.Education;
import com.growlink.backend.entity.Interest;
import com.growlink.backend.entity.Skill;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserInterest;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserSkill;
import com.growlink.backend.entity.WorkExperience;
import com.growlink.backend.exception.DuplicateResourceException;
import com.growlink.backend.exception.InvalidProfileDataException;
import com.growlink.backend.exception.ResourceNotFoundException;
import com.growlink.backend.repository.EducationRepository;
import com.growlink.backend.repository.InterestRepository;
import com.growlink.backend.repository.SkillRepository;
import com.growlink.backend.repository.UserInterestRepository;
import com.growlink.backend.repository.UserProfileRepository;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.repository.UserSkillRepository;
import com.growlink.backend.repository.WorkExperienceRepository;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final EducationRepository educationRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final WorkExperienceRepository experienceRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;

    public ProfileService(UserRepository userRepository, UserProfileRepository profileRepository, EducationRepository educationRepository, SkillRepository skillRepository, UserSkillRepository userSkillRepository, WorkExperienceRepository experienceRepository, InterestRepository interestRepository, UserInterestRepository userInterestRepository) {
        this.userRepository = userRepository; this.profileRepository = profileRepository; this.educationRepository = educationRepository; this.skillRepository = skillRepository; this.userSkillRepository = userSkillRepository; this.experienceRepository = experienceRepository; this.interestRepository = interestRepository; this.userInterestRepository = userInterestRepository;
    }

    @Transactional
    public ProfileResponse getProfile(String email) { return ProfileResponse.from(getOrCreateProfile(currentUser(email))); }

    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        UserProfile profile = getOrCreateProfile(currentUser(email));
        profile.setProfileImage(clean(request.profileImage())); profile.setBio(clean(request.bio())); profile.setDateOfBirth(request.dateOfBirth()); profile.setPhone(clean(request.phone())); profile.setLocation(clean(request.location())); profile.setCountry(clean(request.country())); profile.setWebsite(clean(request.website())); profile.setLinkedinUrl(clean(request.linkedinUrl())); profile.setGithubUrl(clean(request.githubUrl()));
        return ProfileResponse.from(profileRepository.save(profile));
    }

    @Transactional
    public EducationResponse addEducation(String email, EducationRequest request) { validateYears(request.startYear(), request.endYear(), request.currentlyStudying()); Education e = new Education(); apply(e, currentUser(email), request); return EducationResponse.from(educationRepository.save(e)); }
    @Transactional(readOnly = true)
    public List<EducationResponse> getEducation(String email) { return educationRepository.findByUserEmailIgnoreCaseOrderByStartYearDesc(email).stream().map(EducationResponse::from).toList(); }
    @Transactional
    public EducationResponse updateEducation(String email, Long id, EducationRequest request) { validateYears(request.startYear(), request.endYear(), request.currentlyStudying()); Education e = educationRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Education record")); apply(e, e.getUser(), request); return EducationResponse.from(educationRepository.save(e)); }
    @Transactional
    public void deleteEducation(String email, Long id) { educationRepository.delete(educationRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Education record"))); }

    @Transactional
    public SkillResponse addSkill(String email, SkillRequest request) { User user = currentUser(email); String name = request.name().trim(); if (userSkillRepository.existsByUserEmailIgnoreCaseAndSkillNameIgnoreCase(email, name)) throw new DuplicateResourceException("This skill is already assigned to your profile"); Skill skill = skillRepository.findByNameIgnoreCase(name).orElseGet(() -> { Skill created = new Skill(); created.setName(name); return created; }); skill.setDescription(clean(request.skillDescription())); skill.setCategory(clean(request.category())); skillRepository.save(skill); UserSkill userSkill = new UserSkill(); apply(userSkill, user, skill, request); return SkillResponse.from(userSkillRepository.save(userSkill)); }
    @Transactional(readOnly = true)
    public List<SkillResponse> getSkills(String email) { return userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(email).stream().map(SkillResponse::from).toList(); }
    @Transactional
    public SkillResponse updateSkill(String email, Long id, SkillRequest request) { UserSkill userSkill = userSkillRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Skill assignment")); String name = request.name().trim(); if (!userSkill.getSkill().getName().equalsIgnoreCase(name) && userSkillRepository.existsByUserEmailIgnoreCaseAndSkillNameIgnoreCase(email, name)) throw new DuplicateResourceException("This skill is already assigned to your profile"); Skill skill = skillRepository.findByNameIgnoreCase(name).orElse(userSkill.getSkill()); skill.setName(name); skill.setDescription(clean(request.skillDescription())); skill.setCategory(clean(request.category())); skillRepository.save(skill); apply(userSkill, userSkill.getUser(), skill, request); return SkillResponse.from(userSkillRepository.save(userSkill)); }
    @Transactional
    public void deleteSkill(String email, Long id) { userSkillRepository.delete(userSkillRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Skill assignment"))); }

    @Transactional
    public ExperienceResponse addExperience(String email, ExperienceRequest request) { validateDates(request.startDate(), request.endDate(), request.currentlyWorking()); WorkExperience e = new WorkExperience(); apply(e, currentUser(email), request); return ExperienceResponse.from(experienceRepository.save(e)); }
    @Transactional(readOnly = true)
    public List<ExperienceResponse> getExperience(String email) { return experienceRepository.findByUserEmailIgnoreCaseOrderByStartDateDesc(email).stream().map(ExperienceResponse::from).toList(); }
    @Transactional
    public ExperienceResponse updateExperience(String email, Long id, ExperienceRequest request) { validateDates(request.startDate(), request.endDate(), request.currentlyWorking()); WorkExperience e = experienceRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Work experience")); apply(e, e.getUser(), request); return ExperienceResponse.from(experienceRepository.save(e)); }
    @Transactional
    public void deleteExperience(String email, Long id) { experienceRepository.delete(experienceRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Work experience"))); }

    @Transactional(readOnly = true)
    public List<InterestResponse> getInterests() { return interestRepository.findAllByOrderByNameAsc().stream().map(InterestResponse::from).toList(); }
    @Transactional(readOnly = true)
    public List<InterestResponse> getUserInterests(String email) { return userInterestRepository.findByUserEmailIgnoreCaseOrderByInterestNameAsc(email).stream().map(link -> InterestResponse.from(link.getInterest())).toList(); }
    @Transactional
    public List<InterestResponse> updateInterests(String email, Set<Long> interestIds) { User user = currentUser(email); List<Interest> selected = interestRepository.findAllById(interestIds); if (selected.size() != interestIds.size()) throw new InvalidProfileDataException("One or more selected interests do not exist"); userInterestRepository.deleteByUserEmailIgnoreCase(email); userInterestRepository.saveAll(selected.stream().map(interest -> { UserInterest link = new UserInterest(); link.setUser(user); link.setInterest(interest); return link; }).toList()); return selected.stream().map(InterestResponse::from).toList(); }

    private User currentUser(String email) { return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> missing("User")); }
    private UserProfile getOrCreateProfile(User user) { return profileRepository.findByUserEmailIgnoreCase(user.getEmail()).orElseGet(() -> { UserProfile p = new UserProfile(); p.setUser(user); return profileRepository.save(p); }); }
    private void apply(Education e, User user, EducationRequest r) { e.setUser(user); e.setInstitutionName(r.institutionName().trim()); e.setQualification(r.qualification().trim()); e.setFieldOfStudy(clean(r.fieldOfStudy())); e.setStartYear(r.startYear()); e.setEndYear(r.endYear()); e.setCurrentlyStudying(r.currentlyStudying()); e.setDescription(clean(r.description())); }
    private void apply(UserSkill u, User user, Skill skill, SkillRequest r) { u.setUser(user); u.setSkill(skill); u.setProficiencyLevel(r.proficiencyLevel()); u.setYearsOfExperience(r.yearsOfExperience()); u.setDescription(clean(r.description())); }
    private void apply(WorkExperience e, User user, ExperienceRequest r) { e.setUser(user); e.setJobTitle(r.jobTitle().trim()); e.setOrganization(r.organization().trim()); e.setEmploymentType(clean(r.employmentType())); e.setStartDate(r.startDate()); e.setEndDate(r.endDate()); e.setCurrentlyWorking(r.currentlyWorking()); e.setDescription(clean(r.description())); }
    private void validateYears(Integer start, Integer end, boolean current) { if (start != null && end != null && end < start) throw new InvalidProfileDataException("End year must be after start year"); if (current && end != null) throw new InvalidProfileDataException("Currently studying records cannot have an end year"); }
    private void validateDates(LocalDate start, LocalDate end, boolean current) { if (start != null && end != null && end.isBefore(start)) throw new InvalidProfileDataException("End date must be after start date"); if (current && end != null) throw new InvalidProfileDataException("Current roles cannot have an end date"); }
    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private ResourceNotFoundException missing(String resource) { return new ResourceNotFoundException(resource + " was not found"); }
}
