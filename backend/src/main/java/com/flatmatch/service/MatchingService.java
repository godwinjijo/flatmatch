package com.flatmatch.service;

import com.flatmatch.dto.MatchResponse;
import com.flatmatch.model.Swipe;
import com.flatmatch.model.UserProfile;
import com.flatmatch.repository.SwipeRepository;
import com.flatmatch.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * The compatibility engine — THE resume highlight of this project.
 *
 * Strategy:
 *   1. HARD FILTERS  (candidate is excluded, not just low-scored):
 *        - they violate one of MY dealbreakers
 *        - our budget ranges don't overlap at all
 *   2. WEIGHTED DISTANCE on lifestyle sliders (0..100, lower distance = higher score):
 *        cleanliness 32 pts | social 32 pts | party 24 pts | schedule 12 pts
 *   3. BONUS: shared interests, +3 each, capped at +10 (interests are nice-to-have,
 *      not a compatibility basis — lifestyle is what causes flatmate fights).
 *
 * Score is intentionally asymmetric: MY dealbreakers eliminate people, but THEIR
 * dealbreakers don't (they'll filter me out on their side — same logic, mirrored).
 */
@Service
public class MatchingService {

    private final UserProfileRepository repository;
    private final SwipeRepository swipeRepository;

    public MatchingService(UserProfileRepository repository, SwipeRepository swipeRepository) {
        this.repository = repository;
        this.swipeRepository = swipeRepository;
    }

    /**
     * Loads "me" and everyone else from the DB, then ranks them.
     * People I already swiped on never reappear in the deck — otherwise the
     * next swipe would bounce off the "already swiped" rule.
     */
    @Transactional(readOnly = true)
    public List<MatchResponse> getMatches(Long userId) {
        UserProfile me = repository.findById(userId)
                .orElseThrow(() -> new com.flatmatch.exception.ResourceNotFoundException("Profile " + userId + " not found"));
        Set<Long> alreadySwiped = new HashSet<>();
        for (Swipe s : swipeRepository.findBySwiperId(userId)) {
            alreadySwiped.add(s.getSwipeeId());
        }
        List<UserProfile> fresh = repository.findAll().stream()
                .filter(p -> !alreadySwiped.contains(p.getId()))
                .toList();
        return findMatches(me, fresh);
    }

    /** Maps a dealbreaker tag to "does the candidate violate it?" */
    private static final Map<String, Predicate<UserProfile>> DEALBREAKER_RULES = Map.of(
            "smoking",          p -> Boolean.TRUE.equals(p.getSmoker()),
            "loud-parties",     p -> p.getPartyLevel() != null && p.getPartyLevel() >= 4,
            "no-pets-allowed",  p -> Boolean.FALSE.equals(p.getPetsOk()),
            "no-guests-allowed",p -> Boolean.FALSE.equals(p.getGuestsOk()),
            "night-owl",        p -> Boolean.FALSE.equals(p.getNightOwl()),
            "early-riser",      p -> Boolean.TRUE.equals(p.getNightOwl())
    );

    /** Returns ranked candidates for "me", best first. */
    public List<MatchResponse> findMatches(UserProfile me, List<UserProfile> others) {
        List<MatchResponse> results = new ArrayList<>();
        for (UserProfile other : others) {
            if (me.getId() != null && me.getId().equals(other.getId())) continue;
            MatchResponse m = evaluate(me, other);
            if (m != null) results.add(m);
        }
        results.sort((a, b) -> Integer.compare(b.score(), a.score()));
        return results;
    }

    /**
     * Scores one candidate. Returns null when hard-filtered (not a candidate at all).
     * Pure function of the two profiles -> trivially unit-testable, no DB needed.
     */
    public MatchResponse evaluate(UserProfile me, UserProfile other) {
        List<String> violated = violatedDealbreakers(me, other);
        if (!violated.isEmpty() || !budgetsOverlap(me, other)) {
            return null;
        }

        // ---- 2. weighted lifestyle distance: start at 100, subtract penalty points
        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("cleanliness", diff(me.getCleanliness(), other.getCleanliness()) * 8);  // max 32
        breakdown.put("social",      diff(me.getSocialLevel(),  other.getSocialLevel())  * 8);  // max 32
        breakdown.put("party",       diff(me.getPartyLevel(),   other.getPartyLevel())   * 6);  // max 24
        breakdown.put("schedule",    nightOwlMismatch(me, other));                        // 0 or 12

        int penalty = breakdown.values().stream().mapToInt(Integer::intValue).sum();
        int base = Math.max(0, 100 - penalty);

        // ---- 3. shared-interest bonus (capped so it can never outrank lifestyle)
        List<String> shared = sharedInterests(me, other);
        int bonus = Math.min(10, shared.size() * 3);
        int score = Math.min(100, base + bonus);

        return new MatchResponse(
                other.getId(), other.getName(), other.getCity(), other.getAge(),
                other.getBio(), other.getPhotoUrl(), score, shared, List.of(), breakdown
        );
    }

    private List<String> violatedDealbreakers(UserProfile me, UserProfile other) {
        List<String> violated = new ArrayList<>();
        for (String tag : me.getDealbreakers()) {
            Predicate<UserProfile> rule = DEALBREAKER_RULES.get(tag.toLowerCase());
            if (rule != null && rule.test(other)) {
                violated.add(tag);
            }
            // unknown tags are ignored (forward-compatible with new tags)
        }
        return violated;
    }

    private boolean budgetsOverlap(UserProfile me, UserProfile other) {
        if (me.getMaxBudget() == null || other.getMinBudget() == null
                || me.getMinBudget() == null || other.getMaxBudget() == null) {
            return true; // missing budget = don't filter
        }
        return me.getMinBudget() <= other.getMaxBudget()
                && other.getMinBudget() <= me.getMaxBudget();
    }

    private int nightOwlMismatch(UserProfile me, UserProfile other) {
        if (me.getNightOwl() == null || other.getNightOwl() == null) return 0;
        return me.getNightOwl().equals(other.getNightOwl()) ? 0 : 12;
    }

    private List<String> sharedInterests(UserProfile me, UserProfile other) {
        Set<String> mine = me.getInterests();
        List<String> shared = new ArrayList<>();
        for (String interest : other.getInterests()) {
            if (mine.contains(interest)) shared.add(interest);
        }
        return shared;
    }

    /** |a - b| treating nulls as "no data, no penalty" */
    private int diff(Integer a, Integer b) {
        if (a == null || b == null) return 0;
        return Math.abs(a - b);
    }
}
