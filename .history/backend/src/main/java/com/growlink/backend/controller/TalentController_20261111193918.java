package com.growlink.backend.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.growlink.backend.dto.PublicTalentResponse;
import com.growlink.backend.dto.TalentCategoryResponse;
import com.growlink.backend.service.TalentService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/talents")
@Validated
public class TalentController {
    private final TalentService talentService;

    public TalentController(TalentService talentService) {
        this.talentService = talentService;
    }

    @GetMapping
    public Page<PublicTalentResponse> list(
            @RequestParam(required = false) @Size(max = 120) String name,
            @RequestParam(required = false) @Size(max = 120) String skill,
            @RequestParam(required = false) @Size(max = 80) String category,
            @RequestParam(required = false) @Size(max = 120) String location,
            @RequestParam(required = false) @Size(max = 20) String proficiencyLevel,
            @RequestParam(required = false) @Min(0) @Max(80) Integer minimumExperience,
            @RequestParam(required = false) @Size(max = 160) String education,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "name") @Size(max = 20) String sortBy) {
        return talentService.search(name, skill, category, location, proficiencyLevel, minimumExperience,
                education, page, size, sortBy);
    }

    @GetMapping("/search")
    public Page<PublicTalentResponse> search(
            @RequestParam(required = false) @Size(max = 120) String name,
            @RequestParam(required = false) @Size(max = 120) String skill,
            @RequestParam(required = false) @Size(max = 80) String category,
            @RequestParam(required = false) @Size(max = 120) String location,
            @RequestParam(required = false) @Size(max = 20) String proficiencyLevel,
            @RequestParam(required = false) @Min(0) @Max(80) Integer minimumExperience,
            @RequestParam(required = false) @Size(max = 160) String education,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "name") @Size(max = 20) String sortBy) {
        return talentService.search(name, skill, category, location, proficiencyLevel, minimumExperience,
                education, page, size, sortBy);
    }

    @GetMapping("/categories")
    public List<TalentCategoryResponse> categories() {
        return talentService.getCategories();
    }

    @GetMapping("/{id}")
    public PublicTalentResponse get(@PathVariable @Min(1) Long id) {
        return talentService.getPublicProfile(id);
    }
}