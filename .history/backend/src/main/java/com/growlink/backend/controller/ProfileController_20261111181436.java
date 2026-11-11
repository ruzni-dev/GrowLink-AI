package com.growlink.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.growlink.backend.dto.EducationRequest;
import com.growlink.backend.dto.EducationResponse;
import com.growlink.backend.dto.ExperienceRequest;
import com.growlink.backend.dto.ExperienceResponse;
import com.growlink.backend.dto.InterestResponse;
import com.growlink.backend.dto.InterestUpdateRequest;
import com.growlink.backend.dto.ProfileRequest;
import com.growlink.backend.dto.ProfileResponse;
import com.growlink.backend.dto.SkillRequest;
import com.growlink.backend.dto.SkillResponse;
import com.growlink.backend.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profiles")
@Validated
public class ProfileController {
    private final ProfileService profileService;
    public ProfileController(ProfileService profileService) { this.profileService = profileService; }

    @GetMapping("/me") public ProfileResponse getProfile(Authentication auth) { return profileService.getProfile(auth.getName()); }
    @PutMapping("/me") public ProfileResponse updateProfile(Authentication auth, @Valid @RequestBody ProfileRequest request) { return profileService.updateProfile(auth.getName(), request); }

    @PostMapping("/me/education") public ResponseEntity<EducationResponse> addEducation(Authentication auth, @Valid @RequestBody EducationRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addEducation(auth.getName(), request)); }
    @GetMapping("/me/education") public List<EducationResponse> getEducation(Authentication auth) { return profileService.getEducation(auth.getName()); }
    @PutMapping("/me/education/{id}") public EducationResponse updateEducation(Authentication auth, @PathVariable Long id, @Valid @RequestBody EducationRequest request) { return profileService.updateEducation(auth.getName(), id, request); }
    @DeleteMapping("/me/education/{id}") public Map<String, String> deleteEducation(Authentication auth, @PathVariable Long id) { profileService.deleteEducation(auth.getName(), id); return Map.of("message", "Education removed"); }

    @PostMapping("/me/skills") public ResponseEntity<SkillResponse> addSkill(Authentication auth, @Valid @RequestBody SkillRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addSkill(auth.getName(), request)); }
    @GetMapping("/me/skills") public List<SkillResponse> getSkills(Authentication auth) { return profileService.getSkills(auth.getName()); }
    @PutMapping("/me/skills/{id}") public SkillResponse updateSkill(Authentication auth, @PathVariable Long id, @Valid @RequestBody SkillRequest request) { return profileService.updateSkill(auth.getName(), id, request); }
    @DeleteMapping("/me/skills/{id}") public Map<String, String> deleteSkill(Authentication auth, @PathVariable Long id) { profileService.deleteSkill(auth.getName(), id); return Map.of("message", "Skill removed"); }

    @PostMapping("/me/experience") public ResponseEntity<ExperienceResponse> addExperience(Authentication auth, @Valid @RequestBody ExperienceRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(profileService.addExperience(auth.getName(), request)); }
    @GetMapping("/me/experience") public List<ExperienceResponse> getExperience(Authentication auth) { return profileService.getExperience(auth.getName()); }
    @PutMapping("/me/experience/{id}") public ExperienceResponse updateExperience(Authentication auth, @PathVariable Long id, @Valid @RequestBody ExperienceRequest request) { return profileService.updateExperience(auth.getName(), id, request); }
    @DeleteMapping("/me/experience/{id}") public Map<String, String> deleteExperience(Authentication auth, @PathVariable Long id) { profileService.deleteExperience(auth.getName(), id); return Map.of("message", "Experience removed"); }

    @GetMapping("/interests") public List<InterestResponse> getAvailableInterests() { return profileService.getInterests(); }
    @GetMapping("/me/interests") public List<InterestResponse> getUserInterests(Authentication auth) { return profileService.getUserInterests(auth.getName()); }
    @PutMapping("/me/interests") public List<InterestResponse> updateInterests(Authentication auth, @Valid @RequestBody InterestUpdateRequest request) { return profileService.updateInterests(auth.getName(), request.interestIds()); }
}
