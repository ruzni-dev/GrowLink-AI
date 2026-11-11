package com.growlink.backend.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import com.growlink.backend.dto.OpportunityApplicationNotesRequest;
import com.growlink.backend.dto.OpportunityApplicationRequest;
import com.growlink.backend.dto.OpportunityApplicationResponse;
import com.growlink.backend.dto.OpportunityApplicationStatusRequest;
import com.growlink.backend.dto.OpportunityCategoryResponse;
import com.growlink.backend.dto.OpportunityRequest;
import com.growlink.backend.dto.OpportunityResponse;
import com.growlink.backend.dto.OpportunityStatusRequest;
import com.growlink.backend.dto.PagedResponse;
import com.growlink.backend.dto.SavedOpportunityResponse;
import com.growlink.backend.entity.OpportunityApplicationStatus;
import com.growlink.backend.entity.OpportunityStatus;
import com.growlink.backend.service.OpportunityService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/opportunities")
@Validated
public class OpportunityController {
    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @GetMapping
    public PagedResponse<OpportunityResponse> list(
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(required = false) @Size(max = 24) String category,
            @RequestParam(required = false) @Size(max = 160) String location,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) @Size(max = 180) String organization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineBefore,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "recent") @Size(max = 24) String sortBy) {
        return opportunityService.search(q, category, location, remote, organization, deadlineAfter, deadlineBefore, page, size, sortBy);
    }

    @GetMapping("/search")
    public PagedResponse<OpportunityResponse> search(
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(required = false) @Size(max = 24) String category,
            @RequestParam(required = false) @Size(max = 160) String location,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) @Size(max = 180) String organization,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineBefore,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "recent") @Size(max = 24) String sortBy) {
        return opportunityService.search(q, category, location, remote, organization, deadlineAfter, deadlineBefore, page, size, sortBy);
    }

    @GetMapping("/categories")
    public List<OpportunityCategoryResponse> categories() {
        return opportunityService.categories();
    }

    @GetMapping("/{id}")
    public OpportunityResponse get(@PathVariable @Min(1) Long id, Authentication authentication) {
        return opportunityService.getPublicOpportunity(id, authentication == null ? null : authentication.getName());
    }

    @PostMapping
    public ResponseEntity<OpportunityResponse> create(Authentication authentication, @Valid @RequestBody OpportunityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.create(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    public OpportunityResponse update(Authentication authentication, @PathVariable @Min(1) Long id,
            @Valid @RequestBody OpportunityRequest request) {
        return opportunityService.update(authentication.getName(), id, request);
    }

    @PatchMapping("/{id}/status")
    public OpportunityResponse changeStatus(Authentication authentication, @PathVariable @Min(1) Long id,
            @Valid @RequestBody OpportunityStatusRequest request) {
        return opportunityService.changeStatus(authentication.getName(), id, request);
    }

    @GetMapping("/my-opportunities")
    public PagedResponse<OpportunityResponse> myOpportunities(Authentication authentication,
            @RequestParam(required = false) OpportunityStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return opportunityService.myOpportunities(authentication.getName(), status, page, size);
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<SavedOpportunityResponse> save(Authentication authentication, @PathVariable @Min(1) Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(opportunityService.save(authentication.getName(), id));
    }

    @DeleteMapping("/{id}/save")
    public ResponseEntity<Void> removeSaved(Authentication authentication, @PathVariable @Min(1) Long id) {
        opportunityService.removeSaved(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved")
    public PagedResponse<SavedOpportunityResponse> saved(Authentication authentication,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return opportunityService.saved(authentication.getName(), page, size);
    }

    @PostMapping("/{id}/track")
    public ResponseEntity<OpportunityApplicationResponse> track(Authentication authentication,
            @PathVariable @Min(1) Long id, @Valid @RequestBody OpportunityApplicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(opportunityService.track(authentication.getName(), id, request));
    }

    @GetMapping("/applications")
    public PagedResponse<OpportunityApplicationResponse> applications(Authentication authentication,
            @RequestParam(required = false) OpportunityApplicationStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size) {
        return opportunityService.applications(authentication.getName(), status, page, size);
    }

    @PatchMapping("/applications/{id}/status")
    public OpportunityApplicationResponse changeApplicationStatus(Authentication authentication,
            @PathVariable @Min(1) Long id, @Valid @RequestBody OpportunityApplicationStatusRequest request) {
        return opportunityService.changeApplicationStatus(authentication.getName(), id, request);
    }

    @PatchMapping("/applications/{id}/notes")
    public OpportunityApplicationResponse changeApplicationNotes(Authentication authentication,
            @PathVariable @Min(1) Long id, @Valid @RequestBody OpportunityApplicationNotesRequest request) {
        return opportunityService.changeApplicationNotes(authentication.getName(), id, request);
    }
}