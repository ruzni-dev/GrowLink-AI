package com.growlink.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.growlink.backend.dto.MentorshipOfferRequest;
import com.growlink.backend.dto.MentorshipOfferResponse;
import com.growlink.backend.dto.MentorshipOfferStatusRequest;
import com.growlink.backend.dto.MentorshipRequestCreateRequest;
import com.growlink.backend.dto.MentorshipRequestResponse;
import com.growlink.backend.dto.PagedResponse;
import com.growlink.backend.entity.MentorshipOfferStatus;
import com.growlink.backend.entity.MentorshipRequestStatus;
import com.growlink.backend.service.MentorshipService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/mentorship")
@Validated
public class MentorshipController {
    private final MentorshipService mentorshipService;

    public MentorshipController(MentorshipService mentorshipService) {
        this.mentorshipService = mentorshipService;
    }

    @GetMapping("/offers")
    public PagedResponse<MentorshipOfferResponse> findOffers(
            @RequestParam(required = false) @Size(max = 160) String q,
            @RequestParam(required = false) @Size(max = 80) String category,
            @RequestParam(required = false) @Size(max = 120) String location,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "recent") @Size(max = 20) String sortBy) {
        return mentorshipService.findOffers(q, category, location, page, size, sortBy);
    }

    @GetMapping("/offers/{id}")
    public MentorshipOfferResponse getOffer(@PathVariable @Min(1) Long id) {
        return mentorshipService.getPublicOffer(id);
    }

    @PostMapping("/offers")
    public ResponseEntity<MentorshipOfferResponse> createOffer(Authentication authentication,
            @Valid @RequestBody MentorshipOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mentorshipService.createOffer(authentication.getName(), request));
    }

    @PutMapping("/offers/{id}")
    public MentorshipOfferResponse updateOffer(Authentication authentication, @PathVariable @Min(1) Long id,
            @Valid @RequestBody MentorshipOfferRequest request) {
        return mentorshipService.updateOffer(authentication.getName(), id, request);
    }

    @PatchMapping("/offers/{id}/status")
    public MentorshipOfferResponse changeOfferStatus(Authentication authentication, @PathVariable @Min(1) Long id,
            @Valid @RequestBody MentorshipOfferStatusRequest request) {
        return mentorshipService.changeOfferStatus(authentication.getName(), id, request);
    }

    @GetMapping("/my-offers")
    public PagedResponse<MentorshipOfferResponse> myOffers(Authentication authentication,
            @RequestParam(required = false) MentorshipOfferStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {
        return mentorshipService.myOffers(authentication.getName(), status, page, size);
    }

    @PostMapping("/requests")
    public ResponseEntity<MentorshipRequestResponse> createRequest(Authentication authentication,
            @Valid @RequestBody MentorshipRequestCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mentorshipService.createRequest(authentication.getName(), request));
    }

    @GetMapping("/requests/received")
    public PagedResponse<MentorshipRequestResponse> receivedRequests(Authentication authentication,
            @RequestParam(required = false) MentorshipRequestStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {
        return mentorshipService.receivedRequests(authentication.getName(), status, page, size);
    }

    @GetMapping("/requests/sent")
    public PagedResponse<MentorshipRequestResponse> sentRequests(Authentication authentication,
            @RequestParam(required = false) MentorshipRequestStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) int size) {
        return mentorshipService.sentRequests(authentication.getName(), status, page, size);
    }

    @PatchMapping("/requests/{id}/accept")
    public MentorshipRequestResponse accept(Authentication authentication, @PathVariable @Min(1) Long id) {
        return mentorshipService.accept(authentication.getName(), id);
    }

    @PatchMapping("/requests/{id}/reject")
    public MentorshipRequestResponse reject(Authentication authentication, @PathVariable @Min(1) Long id) {
        return mentorshipService.reject(authentication.getName(), id);
    }

    @PatchMapping("/requests/{id}/cancel")
    public MentorshipRequestResponse cancel(Authentication authentication, @PathVariable @Min(1) Long id) {
        return mentorshipService.cancel(authentication.getName(), id);
    }

    @PatchMapping("/requests/{id}/complete")
    public MentorshipRequestResponse complete(Authentication authentication, @PathVariable @Min(1) Long id) {
        return mentorshipService.complete(authentication.getName(), id);
    }
}