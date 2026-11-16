package com.flatmatch.service;

import com.flatmatch.dto.ProfileRequest;
import com.flatmatch.dto.ProfileResponse;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.UserProfile;
import com.flatmatch.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfileService {

    private final UserProfileRepository repository;

    /** Constructor injection — Spring passes the repository automatically. */
    public ProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ProfileResponse create(ProfileRequest req, Long authUserId) {
        if (repository.existsByEmailIgnoreCase(req.email())) {
            throw new IllegalArgumentException("A profile with this email already exists");
        }
        UserProfile p = new UserProfile();
        p.setAuthUserId(authUserId);
        applyRequest(p, req);
        return ProfileResponse.from(repository.save(p));
    }

    public ProfileResponse getById(Long id) {
        return ProfileResponse.from(find(id));
    }

    public List<ProfileResponse> getAll() {
        return repository.findAll().stream().map(ProfileResponse::from).toList();
    }

    @Transactional
    public ProfileResponse update(Long id, ProfileRequest req) {
        UserProfile p = find(id);
        applyRequest(p, req);
        return ProfileResponse.from(repository.save(p));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Profile " + id + " not found");
        }
        repository.deleteById(id);
    }

    private UserProfile find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profile " + id + " not found"));
    }

    /** Copies DTO fields onto the entity — keeps create and update DRY. */
    private void applyRequest(UserProfile p, ProfileRequest req) {
        p.setName(req.name());
        p.setEmail(req.email());
        p.setAge(req.age());
        p.setCity(req.city());
        p.setBio(req.bio());
        p.setPhotoUrl(req.photoUrl());
        p.setCleanliness(req.cleanliness());
        p.setSocialLevel(req.socialLevel());
        p.setPartyLevel(req.partyLevel());
        p.setNightOwl(req.nightOwl());
        p.setCooksRegularly(req.cooksRegularly());
        p.setSmoker(req.smoker());
        p.setPetsOk(req.petsOk());
        p.setGuestsOk(req.guestsOk());
        p.setMinBudget(req.minBudget());
        p.setMaxBudget(req.maxBudget());
        p.setPreferredArea(req.preferredArea());
        p.setDealbreakers(req.dealbreakers() == null ? java.util.Set.of() : req.dealbreakers());
        p.setInterests(req.interests() == null ? java.util.Set.of() : req.interests());
    }
}
