package com.growlink.backend.service;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.growlink.backend.entity.Opportunity;
import com.growlink.backend.entity.OpportunityApplication;
import com.growlink.backend.entity.OpportunityApplicationStatus;
import com.growlink.backend.entity.OpportunityCategory;
import com.growlink.backend.entity.OpportunityStatus;
import com.growlink.backend.entity.SavedOpportunity;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.exception.DuplicateResourceException;
import com.growlink.backend.exception.InvalidProfileDataException;
import com.growlink.backend.exception.ResourceNotFoundException;
import com.growlink.backend.repository.OpportunityApplicationRepository;
import com.growlink.backend.repository.OpportunityRepository;
import com.growlink.backend.repository.SavedOpportunityRepository;
import com.growlink.backend.repository.UserRepository;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunityRepository;
    private final SavedOpportunityRepository savedRepository;
    private final OpportunityApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public OpportunityService(OpportunityRepository opportunityRepository, SavedOpportunityRepository savedRepository,
            OpportunityApplicationRepository applicationRepository, UserRepository userRepository) {
        this.opportunityRepository = opportunityRepository;
        this.savedRepository = savedRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<OpportunityResponse> search(String keyword, String categoryValue, String location,
            Boolean remote, String organization, LocalDate deadlineAfter, LocalDate deadlineBefore,
            int page, int size, String sortBy) {
        if (deadlineAfter != null && deadlineBefore != null && deadlineAfter.isAfter(deadlineBefore)) {
            throw new InvalidProfileDataException("deadlineAfter must be on or before deadlineBefore");
        }
        OpportunityCategory category = parseCategory(categoryValue);
        Pageable pageable = PageRequest.of(page, size, sort(sortBy));
        Page<Opportunity> opportunities = opportunityRepository.findAll(
                OpportunitySpecifications.discover(keyword, category, location, remote, organization, deadlineAfter, deadlineBefore),
                pageable);
        return PagedResponse.from(opportunities.map(opportunity -> response(opportunity, null)));
    }

    @Transactional(readOnly = true)
    public OpportunityResponse getPublicOpportunity(Long id, String viewerEmail) {
        Opportunity opportunity = findDiscoverable(id);
        return response(opportunity, viewerEmail);
    }

    @Transactional
    public OpportunityResponse create(String email, OpportunityRequest request) {
        User user = activeUser(email);
        validateRequest(request);
        OpportunityStatus status = request.status() == null ? OpportunityStatus.DRAFT : request.status();
        if (status != OpportunityStatus.DRAFT && status != OpportunityStatus.PUBLISHED) {
            throw new InvalidProfileDataException("New opportunities must be created as DRAFT or PUBLISHED");
        }
        if (status == OpportunityStatus.PUBLISHED) validatePublishable(request.deadline());
        Opportunity opportunity = new Opportunity();
        opportunity.setCreatedBy(user);
        apply(opportunity, request);
        opportunity.setStatus(status);
        return response(opportunityRepository.save(opportunity), email);
    }

    @Transactional
    public OpportunityResponse update(String email, Long id, OpportunityRequest request) {
        Opportunity opportunity = ownedOpportunity(email, id);
        if (opportunity.getStatus() == OpportunityStatus.ARCHIVED) {
            throw new InvalidProfileDataException("Archived opportunities cannot be edited");
        }
        validateRequest(request);
        if (opportunity.getStatus() == OpportunityStatus.PUBLISHED) validatePublishable(request.deadline());
        apply(opportunity, request);
        return response(opportunityRepository.save(opportunity), email);
    }

    @Transactional
    public OpportunityResponse changeStatus(String email, Long id, OpportunityStatusRequest request) {
        Opportunity opportunity = ownedOpportunity(email, id);
        OpportunityStatus current = opportunity.getStatus();
        OpportunityStatus next = request.status();
        boolean valid = current == next
                || (current == OpportunityStatus.DRAFT && (next == OpportunityStatus.PUBLISHED || next == OpportunityStatus.ARCHIVED))
                || (current == OpportunityStatus.PUBLISHED && (next == OpportunityStatus.CLOSED || next == OpportunityStatus.ARCHIVED))
                || (current == OpportunityStatus.CLOSED && (next == OpportunityStatus.PUBLISHED || next == OpportunityStatus.ARCHIVED));
        if (!valid) throw new InvalidProfileDataException("This opportunity status transition is not allowed");
        if (next == OpportunityStatus.PUBLISHED) validatePublishable(opportunity.getDeadline());
        opportunity.setStatus(next);
        return response(opportunityRepository.save(opportunity), email);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OpportunityResponse> myOpportunities(String email, OpportunityStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
        Page<Opportunity> opportunities = status == null
                ? opportunityRepository.findByCreatedByEmailIgnoreCase(email, pageable)
                : opportunityRepository.findByCreatedByEmailIgnoreCaseAndStatus(email, status, pageable);
        return PagedResponse.from(opportunities.map(opportunity -> response(opportunity, email)));
    }

    @Transactional
    public SavedOpportunityResponse save(String email, Long opportunityId) {
        User user = activeUser(email);
        Opportunity opportunity = findDiscoverable(opportunityId);
        if (savedRepository.existsByUserEmailIgnoreCaseAndOpportunityId(email, opportunityId)) {
            throw new DuplicateResourceException("This opportunity is already saved");
        }
        SavedOpportunity saved = new SavedOpportunity();
        saved.setUser(user);
        saved.setOpportunity(opportunity);
        saved = savedRepository.save(saved);
        return savedResponse(saved, email);
    }

    @Transactional
    public void removeSaved(String email, Long opportunityId) {
        SavedOpportunity saved = savedRepository.findByUserEmailIgnoreCaseAndOpportunityId(email, opportunityId)
                .orElseThrow(() -> missing("Saved opportunity"));
        savedRepository.delete(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<SavedOpportunityResponse> saved(String email, int page, int size) {
        Page<SavedOpportunity> savedItems = savedRepository.findByUserEmailIgnoreCase(email,
                PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"))));
        return PagedResponse.from(savedItems.map(item -> savedResponse(item, email)));
    }

    @Transactional
    public OpportunityApplicationResponse track(String email, Long opportunityId, OpportunityApplicationRequest request) {
        User user = activeUser(email);
        Opportunity opportunity = findDiscoverable(opportunityId);
        if (applicationRepository.existsByUserEmailIgnoreCaseAndOpportunityId(email, opportunityId)) {
            throw new DuplicateResourceException("Application tracking already exists for this opportunity");
        }
        OpportunityApplicationStatus initialStatus = request.status() == null
                ? OpportunityApplicationStatus.INTERESTED : request.status();
        if (initialStatus != OpportunityApplicationStatus.INTERESTED && initialStatus != OpportunityApplicationStatus.APPLIED) {
            throw new InvalidProfileDataException("A tracking record must start as INTERESTED or APPLIED");
        }
        OpportunityApplication application = new OpportunityApplication();
        application.setUser(user);
        application.setOpportunity(opportunity);
        application.setStatus(initialStatus);
        application.setNotes(clean(request.notes()));
        if (initialStatus == OpportunityApplicationStatus.APPLIED) application.setAppliedAt(Instant.now());
        return applicationResponse(applicationRepository.save(application), email);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OpportunityApplicationResponse> applications(String email, OpportunityApplicationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
        Page<OpportunityApplication> filtered = status == null
            ? applicationRepository.findByUserEmailIgnoreCase(email, pageable)
            : applicationRepository.findByUserEmailIgnoreCaseAndStatus(email, status, pageable);
        return PagedResponse.from(filtered.map(application -> applicationResponse(application, email)));
    }

    @Transactional
    public OpportunityApplicationResponse changeApplicationStatus(String email, Long id, OpportunityApplicationStatusRequest request) {
        OpportunityApplication application = ownedApplication(email, id);
        application.setStatus(request.status());
        if (request.status() == OpportunityApplicationStatus.APPLIED && application.getAppliedAt() == null) {
            application.setAppliedAt(Instant.now());
        }
        return applicationResponse(applicationRepository.save(application), email);
    }

    @Transactional
    public OpportunityApplicationResponse changeApplicationNotes(String email, Long id, OpportunityApplicationNotesRequest request) {
        OpportunityApplication application = ownedApplication(email, id);
        application.setNotes(clean(request.notes()));
        return applicationResponse(applicationRepository.save(application), email);
    }

    public List<OpportunityCategoryResponse> categories() {
        return Arrays.stream(OpportunityCategory.values())
                .map(category -> new OpportunityCategoryResponse(category, label(category)))
                .toList();
    }

    private void apply(Opportunity opportunity, OpportunityRequest request) {
        opportunity.setTitle(request.title().trim());
        opportunity.setDescription(request.description().trim());
        opportunity.setOrganizationName(request.organizationName().trim());
        opportunity.setCategory(request.category());
        opportunity.setOpportunityType(clean(request.opportunityType()));
        opportunity.setLocation(clean(request.location()));
        opportunity.setRemote(Boolean.TRUE.equals(request.remote()));
        opportunity.setEligibility(clean(request.eligibility()));
        opportunity.setRequiredSkills(normalizeSkills(request.requiredSkills()));
        opportunity.setQualification(clean(request.qualification()));
        opportunity.setApplicationUrl(validateApplicationUrl(request.applicationUrl()));
        opportunity.setContactInformation(clean(request.contactInformation()));
        opportunity.setStartDate(request.startDate());
        opportunity.setDeadline(request.deadline());
    }

    private void validateRequest(OpportunityRequest request) {
        if (request.startDate() != null && request.deadline() != null && request.deadline().isBefore(request.startDate())) {
            throw new InvalidProfileDataException("Deadline must be on or after the start date");
        }
        if (request.remote() == Boolean.FALSE && (request.location() == null || request.location().isBlank())) {
            throw new InvalidProfileDataException("Location is required for on-site opportunities");
        }
    }

    private void validatePublishable(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new InvalidProfileDataException("An expired opportunity cannot be published");
        }
    }

    private String validateApplicationUrl(String value) {
        String cleaned = clean(value);
        if (cleaned == null) return null;
        try {
            URI uri = URI.create(cleaned);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidProfileDataException("Application URL must be a valid HTTP or HTTPS link");
        }
        return cleaned;
    }

    private Set<String> normalizeSkills(Set<String> values) {
        if (values == null || values.isEmpty()) return Set.of();
        TreeSet<String> normalized = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        values.stream().map(String::trim).filter(value -> !value.isBlank()).forEach(normalized::add);
        return normalized;
    }

    private Opportunity findDiscoverable(Long id) {
        return opportunityRepository.findOne(OpportunitySpecifications.discover(null, null, null, null, null, null, null)
                        .and((root, query, builder) -> builder.equal(root.get("id"), id)))
                .orElseThrow(() -> missing("Opportunity"));
    }

    private Opportunity ownedOpportunity(String email, Long id) {
        Opportunity opportunity = opportunityRepository.findById(id).orElseThrow(() -> missing("Opportunity"));
        if (!opportunity.getCreatedBy().getEmail().equalsIgnoreCase(email)) throw missing("Opportunity");
        return opportunity;
    }

    private OpportunityApplication ownedApplication(String email, Long id) {
        return applicationRepository.findByIdAndUserEmailIgnoreCase(id, email).orElseThrow(() -> missing("Application tracking record"));
    }

    private User activeUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> missing("User"));
        if (user.getStatus() != UserStatus.ACTIVE) throw missing("User");
        return user;
    }

    private SavedOpportunityResponse savedResponse(SavedOpportunity saved, String viewerEmail) {
        return new SavedOpportunityResponse(saved.getId(), saved.getCreatedAt(), response(saved.getOpportunity(), viewerEmail));
    }

    private OpportunityApplicationResponse applicationResponse(OpportunityApplication application, String viewerEmail) {
        return new OpportunityApplicationResponse(application.getId(), response(application.getOpportunity(), viewerEmail),
                application.getStatus(), application.getNotes(), application.getAppliedAt(),
                application.getCreatedAt(), application.getUpdatedAt());
    }

    private OpportunityResponse response(Opportunity opportunity, String viewerEmail) {
        boolean owner = viewerEmail != null && opportunity.getCreatedBy().getEmail().equalsIgnoreCase(viewerEmail);
        return new OpportunityResponse(opportunity.getId(), opportunity.getTitle(), opportunity.getDescription(),
                opportunity.getOrganizationName(), opportunity.getCategory(), opportunity.getOpportunityType(),
                opportunity.getLocation(), opportunity.isRemote(), opportunity.getEligibility(),
                Set.copyOf(opportunity.getRequiredSkills()), opportunity.getQualification(), opportunity.getApplicationUrl(),
                owner ? opportunity.getContactInformation() : null, opportunity.getStartDate(), opportunity.getDeadline(),
                opportunity.getStatus(), opportunity.getCreatedAt(), opportunity.getUpdatedAt());
    }

    private OpportunityCategory parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try { return OpportunityCategory.valueOf(value.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { throw new InvalidProfileDataException("Unknown opportunity category"); }
    }

    private Sort sort(String sortBy) {
        String value = sortBy == null || sortBy.isBlank() ? "recent" : sortBy.trim().toLowerCase(Locale.ROOT);
        return switch (value) {
            case "recent", "newest" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case "deadline" -> Sort.by(Sort.Order.asc("deadline"), Sort.Order.desc("id"));
            case "title" -> Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
            case "organization" -> Sort.by(Sort.Order.asc("organizationName"), Sort.Order.asc("title"));
            default -> throw new InvalidProfileDataException("sortBy must be recent, deadline, title, or organization");
        };
    }

    private String label(OpportunityCategory category) {
        return switch (category) {
            case JOB -> "Job";
            case INTERNSHIP -> "Internship";
            case SCHOLARSHIP -> "Scholarship";
            case TRAINING -> "Training";
            case VOLUNTEERING -> "Volunteering";
            case COMPETITION -> "Competition";
            case HACKATHON -> "Hackathon";
            case EVENT -> "Event";
            case OTHER -> "Other";
        };
    }

    private String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private ResourceNotFoundException missing(String resource) { return new ResourceNotFoundException(resource + " was not found"); }
}