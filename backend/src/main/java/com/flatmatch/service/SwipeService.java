package com.flatmatch.service;

import com.flatmatch.dto.SwipeRequest;
import com.flatmatch.dto.SwipeResponse;
import com.flatmatch.exception.ResourceNotFoundException;
import com.flatmatch.model.Match;
import com.flatmatch.model.Swipe;
import com.flatmatch.model.SwipeDecision;
import com.flatmatch.repository.MatchRepository;
import com.flatmatch.repository.SwipeRepository;
import com.flatmatch.repository.UserProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The swipe flow:
 *   1. validate (no self-swipe, both profiles exist, no duplicate swipe)
 *   2. save the decision
 *   3. if it's a LIKE/SUPER_LIKE and the OTHER side already liked me -> MATCH!
 *      (created only once, guarded by the ordered-pair unique constraint)
 */
@Service
public class SwipeService {

    private final SwipeRepository swipeRepository;
    private final MatchRepository matchRepository;
    private final UserProfileRepository profileRepository;

    public SwipeService(SwipeRepository swipeRepository,
                        MatchRepository matchRepository,
                        UserProfileRepository profileRepository) {
        this.swipeRepository = swipeRepository;
        this.matchRepository = matchRepository;
        this.profileRepository = profileRepository;
    }

    @Transactional
    public SwipeResponse recordSwipe(SwipeRequest request) {
        Long swiperId = request.swiperId();
        Long swipeeId = request.swipeeId();

        if (swiperId.equals(swipeeId)) {
            throw new IllegalArgumentException("You cannot swipe on yourself");
        }
        if (!profileRepository.existsById(swiperId)) {
            throw new ResourceNotFoundException("Profile " + swiperId + " not found");
        }
        if (!profileRepository.existsById(swipeeId)) {
            throw new ResourceNotFoundException("Profile " + swipeeId + " not found");
        }
        if (swipeRepository.existsBySwiperIdAndSwipeeId(swiperId, swipeeId)) {
            throw new IllegalArgumentException("Already swiped on this profile");
        }

        Swipe swipe = swipeRepository.save(new Swipe(swiperId, swipeeId, request.decision()));

        // PASS never creates a match, no matter what the other side did
        if (request.decision() == SwipeDecision.PASS) {
            return SwipeResponse.notMatched(swipe.getId());
        }

        // Did the other side already like me?
        boolean mutual = swipeRepository
                .findBySwiperIdAndSwipeeId(swipeeId, swiperId)   // their swipe on me
                .map(theirSwipe -> theirSwipe.getDecision() != SwipeDecision.PASS)
                .orElse(false);

        if (!mutual) {
            return SwipeResponse.notMatched(swipe.getId());
        }

        Match match = findOrCreateMatch(swiperId, swipeeId);
        return SwipeResponse.matched(swipe.getId(), match.getId());
    }

    private Match findOrCreateMatch(Long a, Long b) {
        return matchRepository
                .findByUserAIdAndUserBId(Math.min(a, b), Math.max(a, b))
                .orElseGet(() -> matchRepository.save(new Match(a, b)));
    }
}
