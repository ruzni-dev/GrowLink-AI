package com.growlink.backend.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.growlink.backend.dto.MentorshipOfferRequest;
import com.growlink.backend.dto.MentorshipOfferResponse;
import com.growlink.backend.dto.MentorshipOfferSkillResponse;
import com.growlink.backend.dto.MentorshipOfferStatusRequest;
import com.growlink.backend.dto.MentorshipRequestCreateRequest;
import com.growlink.backend.dto.MentorshipRequestResponse;
import com.growlink.backend.dto.PagedResponse;
import com.growlink.backend.entity.MentorshipMode;
import com.growlink.backend.entity.MentorshipOffer;
import com.growlink.backend.entity.MentorshipOfferStatus;
import com.growlink.backend.entity.MentorshipRequest;
import com.growlink.backend.entity.MentorshipRequestStatus;
import com.growlink.backend.entity.ProfileVisibility;
import com.growlink.backend.entity.Skill;
import com.growlink.backend.entity.User;
import com.growlink.backend.entity.UserProfile;
import com.growlink.backend.entity.UserSkill;
import com.growlink.backend.entity.UserStatus;
import com.growlink.backend.exception.DuplicateResourceException;
import com.growlink.backend.exception.InvalidProfileDataException;
import com.growlink.backend.exception.ResourceNotFoundException;
import com.growlink.backend.repository.MentorshipOfferRepository;
import com.growlink.backend.repository.MentorshipRequestRepository;
import com.growlink.backend.repository.UserProfileRepository;
import com.growlink.backend.repository.UserRepository;
import com.growlink.backend.repository.UserSkillRepository;

@Service
public class MentorshipService {
    private final MentorshipOfferRepository offerRepository;
    private final MentorshipRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserSkillRepository userSkillRepository;

    public MentorshipService(MentorshipOfferRepository offerRepository,
            MentorshipRequestRepository requestRepository, UserRepository userRepository,
            UserProfileRepository profileRepository, UserSkillRepository userSkillRepository) {
        this.offerRepository = offerRepository;
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userSkillRepository = userSkillRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorshipOfferResponse> findOffers(String query, String category, String location,
            int page, int size, String sortBy) {
        return findOffers(query, category, location, page, size, sortBy, null);
        }

        @Transactional(readOnly = true)
        public PagedResponse<MentorshipOfferResponse> findOffers(String query, String category, String location,
            int page, int size, String sortBy, Long excludedMentorId) {
        Pageable pageable = PageRequest.of(page, size, offerSort(sortBy));
        var specification = MentorshipSpecifications.publicOffers(query, category, location);
        if (excludedMentorId != null) specification = specification.and(MentorshipSpecifications.excludingMentor(excludedMentorId));
        Page<MentorshipOffer> offers = offerRepository.findAll(
            specification, pageable);
        Map<Long, UserProfile> profiles = profilesFor(offers.getContent().stream().map(MentorshipOffer::getMentor).toList());
        Map<Long, List<UserSkill>> skills = skillsFor(offers.getContent().stream().map(MentorshipOffer::getMentor).toList());
        return PagedResponse.from(offers.map(offer -> offerResponse(offer, profiles.get(offer.getMentor().getId()),
                skills.getOrDefault(offer.getMentor().getId(), List.of()))));
    }

    @Transactional(readOnly = true)
    public MentorshipOfferResponse getPublicOffer(Long id) {
        MentorshipOffer offer = offerRepository.findOne(MentorshipSpecifications.publicOffers(null, null, null)
                .and((root, query, builder) -> builder.equal(root.get("id"), id)))
                .orElseThrow(() -> missing("Mentorship offer"));
        UserProfile profile = profileRepository.findByUserEmailIgnoreCase(offer.getMentor().getEmail()).orElseThrow(() -> missing("Mentorship offer"));
        List<UserSkill> skills = userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(offer.getMentor().getEmail());
        return offerResponse(offer, profile, skills);
    }

    @Transactional(readOnly = true)
    public List<MentorshipOfferResponse> publicOffersForMentor(Long mentorId) {
        User mentor = userRepository.findById(mentorId).filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> missing("Mentor"));
        if (!isPublic(mentor)) throw missing("Mentor");
        List<MentorshipOffer> offers = offerRepository.findByMentor_IdAndStatusOrderByCreatedAtDesc(mentorId, MentorshipOfferStatus.ACTIVE);
        if (offers.isEmpty()) throw missing("Mentor");
        UserProfile profile = profileRepository.findByUserEmailIgnoreCase(mentor.getEmail()).orElseThrow(() -> missing("Mentor"));
        List<UserSkill> skills = userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(mentor.getEmail());
        return offers.stream().map(offer -> offerResponse(offer, profile, skills)).toList();
    }

    @Transactional
    public MentorshipOfferResponse createOffer(String email, MentorshipOfferRequest request) {
        User mentor = activeUser(email);
        MentorshipOffer offer = new MentorshipOffer();
        offer.setMentor(mentor);
        apply(offer, request, mentor);
        return ownOfferResponse(offerRepository.save(offer));
    }

    @Transactional
    public MentorshipOfferResponse updateOffer(String email, Long id, MentorshipOfferRequest request) {
        MentorshipOffer offer = ownedOffer(email, id);
        if (offer.getStatus() == MentorshipOfferStatus.CLOSED) {
            throw new InvalidProfileDataException("Closed mentorship offers cannot be edited");
        }
        apply(offer, request, offer.getMentor());
        return ownOfferResponse(offerRepository.save(offer));
    }

    @Transactional
    public MentorshipOfferResponse changeOfferStatus(String email, Long id, MentorshipOfferStatusRequest request) {
        MentorshipOffer offer = ownedOffer(email, id);
        MentorshipOfferStatus current = offer.getStatus();
        MentorshipOfferStatus next = request.status();
        boolean valid = current == next
                || (current == MentorshipOfferStatus.ACTIVE && (next == MentorshipOfferStatus.PAUSED || next == MentorshipOfferStatus.CLOSED))
                || (current == MentorshipOfferStatus.PAUSED && (next == MentorshipOfferStatus.ACTIVE || next == MentorshipOfferStatus.CLOSED));
        if (!valid) throw new InvalidProfileDataException("This mentorship offer status transition is not allowed");
        offer.setStatus(next);
        return ownOfferResponse(offerRepository.save(offer));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorshipOfferResponse> myOffers(String email, MentorshipOfferStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt"), Sort.Order.desc("id")));
        Page<MentorshipOffer> offers = status == null
                ? offerRepository.findByMentorEmailIgnoreCase(email, pageable)
                : offerRepository.findByMentorEmailIgnoreCaseAndStatus(email, status, pageable);
        Map<Long, UserProfile> profiles = profilesFor(offers.getContent().stream().map(MentorshipOffer::getMentor).toList());
        Map<Long, List<UserSkill>> skills = skillsFor(offers.getContent().stream().map(MentorshipOffer::getMentor).toList());
        return PagedResponse.from(offers.map(offer -> offerResponse(offer, profiles.get(offer.getMentor().getId()),
                skills.getOrDefault(offer.getMentor().getId(), List.of()))));
    }

    @Transactional
    public MentorshipRequestResponse createRequest(String email, MentorshipRequestCreateRequest request) {
        User mentee = activeUser(email);
        MentorshipOffer offer = offerRepository.findByIdForUpdate(request.offerId()).orElseThrow(() -> missing("Mentorship offer"));
        if (offer.getStatus() != MentorshipOfferStatus.ACTIVE || offer.getMentor().getStatus() != UserStatus.ACTIVE
                || !isPublic(offer.getMentor())) {
            throw missing("Mentorship offer");
        }
        if (mentee.getId().equals(offer.getMentor().getId())) {
            throw new InvalidProfileDataException("You cannot request mentorship from yourself");
        }
        if (!modeIsCompatible(offer.getPreferredMode(), request.preferredMode())) {
            throw new InvalidProfileDataException("The selected mode is not available for this mentorship offer");
        }
        if (requestRepository.existsByOfferIdAndMenteeIdAndStatus(offer.getId(), mentee.getId(), MentorshipRequestStatus.PENDING)) {
            throw new DuplicateResourceException("You already have a pending request for this offer");
        }
        MentorshipRequest mentorshipRequest = new MentorshipRequest();
        mentorshipRequest.setOffer(offer);
        mentorshipRequest.setMentor(offer.getMentor());
        mentorshipRequest.setMentee(mentee);
        mentorshipRequest.setMessage(request.message().trim());
        mentorshipRequest.setGoals(request.goals().trim());
        mentorshipRequest.setPreferredMode(request.preferredMode());
        return requestResponse(requestRepository.save(mentorshipRequest));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorshipRequestResponse> receivedRequests(String email, MentorshipRequestStatus status, int page, int size) {
        return requestPage(requestRepository.findReceived(email, status, requestPageable(page, size)));
    }

    @Transactional(readOnly = true)
    public PagedResponse<MentorshipRequestResponse> sentRequests(String email, MentorshipRequestStatus status, int page, int size) {
        return requestPage(requestRepository.findSent(email, status, requestPageable(page, size)));
    }

    @Transactional
    public MentorshipRequestResponse accept(String email, Long id) {
        MentorshipRequest request = requestForMentor(email, id);
        requireStatus(request, MentorshipRequestStatus.PENDING, "Only pending mentorship requests can be accepted");
        request.setStatus(MentorshipRequestStatus.ACCEPTED);
        return requestResponse(requestRepository.save(request));
    }

    @Transactional
    public MentorshipRequestResponse reject(String email, Long id) {
        MentorshipRequest request = requestForMentor(email, id);
        requireStatus(request, MentorshipRequestStatus.PENDING, "Only pending mentorship requests can be rejected");
        request.setStatus(MentorshipRequestStatus.REJECTED);
        return requestResponse(requestRepository.save(request));
    }

    @Transactional
    public MentorshipRequestResponse cancel(String email, Long id) {
        MentorshipRequest request = requestForMentee(email, id);
        requireStatus(request, MentorshipRequestStatus.PENDING, "Only pending mentorship requests can be cancelled");
        request.setStatus(MentorshipRequestStatus.CANCELLED);
        return requestResponse(requestRepository.save(request));
    }

    @Transactional
    public MentorshipRequestResponse complete(String email, Long id) {
        MentorshipRequest request = requestRepository.findById(id).orElseThrow(() -> missing("Mentorship request"));
        boolean participant = sameUser(request.getMentor(), email) || sameUser(request.getMentee(), email);
        if (!participant) throw missing("Mentorship request");
        requireStatus(request, MentorshipRequestStatus.ACCEPTED, "Only accepted mentorships can be completed");
        request.setStatus(MentorshipRequestStatus.COMPLETED);
        return requestResponse(requestRepository.save(request));
    }

    private void apply(MentorshipOffer offer, MentorshipOfferRequest request, User mentor) {
        if (request.preferredMode() != MentorshipMode.ONLINE && isBlank(request.location())) {
            throw new InvalidProfileDataException("A location is required for in-person mentorship");
        }
        List<UserSkill> assignments = userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(mentor.getEmail());
        Map<Long, UserSkill> assignmentsBySkill = assignments.stream()
                .collect(Collectors.toMap(item -> item.getSkill().getId(), Function.identity()));
        if (!assignmentsBySkill.keySet().containsAll(request.skillIds())) {
            throw new InvalidProfileDataException("Offers can only include skills listed on your profile");
        }
        Set<Skill> skills = request.skillIds().stream().map(assignmentsBySkill::get)
                .map(UserSkill::getSkill).collect(Collectors.toSet());
        offer.setTitle(request.title().trim());
        offer.setDescription(request.description().trim());
        offer.setCategory(request.category());
        offer.setSkills(skills);
        offer.setExperience(clean(request.experience()));
        offer.setAvailability(request.availability().trim());
        offer.setPreferredMode(request.preferredMode());
        offer.setLocation(clean(request.location()));
    }

    private MentorshipOffer ownedOffer(String email, Long id) {
        MentorshipOffer offer = offerRepository.findById(id).orElseThrow(() -> missing("Mentorship offer"));
        if (!sameUser(offer.getMentor(), email)) throw missing("Mentorship offer");
        return offer;
    }

    private MentorshipRequest requestForMentor(String email, Long id) {
        MentorshipRequest request = requestRepository.findById(id).orElseThrow(() -> missing("Mentorship request"));
        if (!sameUser(request.getMentor(), email)) throw missing("Mentorship request");
        return request;
    }

    private MentorshipRequest requestForMentee(String email, Long id) {
        MentorshipRequest request = requestRepository.findById(id).orElseThrow(() -> missing("Mentorship request"));
        if (!sameUser(request.getMentee(), email)) throw missing("Mentorship request");
        return request;
    }

    private boolean sameUser(User user, String email) {
        return user.getEmail().equalsIgnoreCase(email);
    }

    private void requireStatus(MentorshipRequest request, MentorshipRequestStatus expected, String message) {
        if (request.getStatus() != expected) throw new InvalidProfileDataException(message);
    }

    private boolean modeIsCompatible(MentorshipMode offerMode, MentorshipMode requestMode) {
        return offerMode == MentorshipMode.BOTH || offerMode == requestMode;
    }

    private User activeUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> missing("User"));
        if (user.getStatus() != UserStatus.ACTIVE) throw missing("User");
        return user;
    }

    private boolean isPublic(User user) {
        return profileRepository.findByUserEmailIgnoreCase(user.getEmail())
                .map(UserProfile::getVisibility).orElse(ProfileVisibility.PRIVATE) == ProfileVisibility.PUBLIC;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String clean(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private MentorshipOfferResponse ownOfferResponse(MentorshipOffer offer) {
        UserProfile profile = profileRepository.findByUserEmailIgnoreCase(offer.getMentor().getEmail()).orElse(null);
        List<UserSkill> skills = userSkillRepository.findByUserEmailIgnoreCaseOrderBySkillNameAsc(offer.getMentor().getEmail());
        return offerResponse(offer, profile, skills);
    }

    private Map<Long, UserProfile> profilesFor(Collection<User> users) {
        if (users.isEmpty()) return Map.of();
        List<Long> ids = users.stream().map(User::getId).distinct().toList();
        return profileRepository.findByUser_IdIn(ids).stream()
                .collect(Collectors.toMap(profile -> profile.getUser().getId(), Function.identity()));
    }

    private Map<Long, List<UserSkill>> skillsFor(Collection<User> users) {
        if (users.isEmpty()) return Map.of();
        List<Long> ids = users.stream().map(User::getId).distinct().toList();
        return userSkillRepository.findByUser_IdInOrderBySkillNameAsc(ids).stream()
                .collect(Collectors.groupingBy(skill -> skill.getUser().getId()));
    }

    private MentorshipOfferResponse offerResponse(MentorshipOffer offer, UserProfile profile, List<UserSkill> assignments) {
        Map<Long, UserSkill> assignmentBySkill = assignments.stream()
                .collect(Collectors.toMap(item -> item.getSkill().getId(), Function.identity(), (first, ignored) -> first));
        List<MentorshipOfferSkillResponse> skills = offer.getSkills().stream()
                .map(skill -> {
                    UserSkill assignment = assignmentBySkill.get(skill.getId());
                    return new MentorshipOfferSkillResponse(skill.getId(), skill.getName(), skill.getCategory(),
                            assignment == null ? null : assignment.getProficiencyLevel().name(),
                            assignment == null ? null : assignment.getYearsOfExperience());
                }).sorted(java.util.Comparator.comparing(MentorshipOfferSkillResponse::name, String.CASE_INSENSITIVE_ORDER)).toList();
        return new MentorshipOfferResponse(offer.getId(), offer.getMentor().getFullName(),
                profile == null ? null : profile.getProfileImage(), profile == null ? null : profile.getBio(),
                profile == null ? offer.getMentor().getLocation() : firstNonBlank(profile.getLocation(), offer.getMentor().getLocation()),
                offer.getTitle(), offer.getDescription(), offer.getCategory(), offer.getCategory().getLabel(), skills,
                offer.getExperience(), offer.getAvailability(), offer.getPreferredMode(), offer.getLocation(),
                offer.getStatus(), offer.getCreatedAt(), offer.getUpdatedAt());
    }

    private PagedResponse<MentorshipRequestResponse> requestPage(Page<MentorshipRequest> requests) {
        return PagedResponse.from(requests.map(this::requestResponse));
    }

    private MentorshipRequestResponse requestResponse(MentorshipRequest request) {
        return new MentorshipRequestResponse(request.getId(), request.getOffer().getId(), request.getOffer().getTitle(),
                request.getMentor().getFullName(), request.getMentee().getFullName(), request.getMessage(),
                request.getGoals(), request.getPreferredMode(), request.getStatus(), request.getCreatedAt(), request.getUpdatedAt());
    }

    private Pageable requestPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
    }

    private Sort offerSort(String sortBy) {
        String value = sortBy == null || sortBy.isBlank() ? "recent" : sortBy.trim().toLowerCase(java.util.Locale.ROOT);
        return switch (value) {
            case "recent", "newest" -> Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
            case "title" -> Sort.by(Sort.Order.asc("title"), Sort.Order.asc("id"));
            default -> throw new InvalidProfileDataException("sortBy must be recent or title");
        };
    }

    private String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    private ResourceNotFoundException missing(String resource) {
        return new ResourceNotFoundException(resource + " was not found");
    }
}