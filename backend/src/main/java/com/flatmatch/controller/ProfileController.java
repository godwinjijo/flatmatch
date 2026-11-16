package com.flatmatch.controller;

import com.flatmatch.dto.MatchResponse;
import com.flatmatch.dto.ProfileRequest;
import com.flatmatch.dto.ProfileResponse;
import com.flatmatch.service.MatchingService;
import com.flatmatch.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for profiles.
 * GET    /api/profiles        -> list all
 * GET    /api/profiles/{id}   -> one profile
 * POST   /api/profiles        -> create (React onboarding form hits this)
 * PUT    /api/profiles/{id}   -> update
 * DELETE /api/profiles/{id}   -> delete
 */
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService service;
    private final MatchingService matchingService;

    public ProfileController(ProfileService service, MatchingService matchingService) {
        this.service = service;
        this.matchingService = matchingService;
    }

    /** Ranked compatible candidates for this profile (the swipe deck's data source). */
    @GetMapping("/{id}/matches")
    public List<MatchResponse> getMatches(@PathVariable Long id) {
        return matchingService.getMatches(id);
    }

    @GetMapping
    public List<ProfileResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ProfileResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponse create(@Valid @RequestBody ProfileRequest request,
                                  @AuthenticationPrincipal Long userId) {
        return service.create(request, userId);
    }

    @PutMapping("/{id}")
    public ProfileResponse update(@PathVariable Long id, @Valid @RequestBody ProfileRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
