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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * The mutual-match decision tree, tested with mocked repositories.
 * Verifies behaviour AND that we don't create matches for one-sided likes.
 */
class SwipeServiceTest {

    private final SwipeRepository swipeRepo = mock(SwipeRepository.class);
    private final MatchRepository matchRepo = mock(MatchRepository.class);
    private final UserProfileRepository profileRepo = mock(UserProfileRepository.class);
    private final SwipeService service = new SwipeService(swipeRepo, matchRepo, profileRepo);

    private SwipeRequest request(Long from, Long to, SwipeDecision d) {
        return new SwipeRequest(from, to, d);
    }

    private void bothProfilesExist() {
        when(profileRepo.existsById(1L)).thenReturn(true);
        when(profileRepo.existsById(2L)).thenReturn(true);
    }

    @Test
    @DisplayName("LIKE + other side already liked -> creates a match")
    void mutualLikeCreatesMatch() {
        bothProfilesExist();
        when(swipeRepo.existsBySwiperIdAndSwipeeId(2L, 1L)).thenReturn(false);
        // I (2) swipe on 1, who already liked me
        when(swipeRepo.findBySwiperIdAndSwipeeId(1L, 2L))
                .thenReturn(Optional.of(new Swipe(1L, 2L, SwipeDecision.LIKE)));
        when(swipeRepo.save(any(Swipe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepo.findByUserAIdAndUserBId(1L, 2L)).thenReturn(Optional.empty());
        when(matchRepo.save(any(Match.class))).thenAnswer(inv -> {
            Match m = inv.getArgument(0);
            m.setId(42L); // simulate what the real database does (assigns an id)
            return m;
        });

        SwipeResponse response = service.recordSwipe(request(2L, 1L, SwipeDecision.LIKE));

        assertTrue(response.matched());
        assertNotNull(response.matchId());
        verify(matchRepo).save(any(Match.class));
    }

    @Test
    @DisplayName("LIKE but other side never swiped -> no match")
    void oneSidedLikeNoMatch() {
        bothProfilesExist();
        when(swipeRepo.existsBySwiperIdAndSwipeeId(1L, 2L)).thenReturn(false);
        when(swipeRepo.findBySwiperIdAndSwipeeId(2L, 1L)).thenReturn(Optional.empty());
        when(swipeRepo.save(any(Swipe.class))).thenAnswer(inv -> inv.getArgument(0));

        SwipeResponse response = service.recordSwipe(request(1L, 2L, SwipeDecision.LIKE));

        assertFalse(response.matched());
        verify(matchRepo, never()).save(any(Match.class));
    }

    @Test
    @DisplayName("PASS never creates a match, even if the other side liked me")
    void passNeverMatches() {
        bothProfilesExist();
        when(swipeRepo.existsBySwiperIdAndSwipeeId(1L, 2L)).thenReturn(false);
        when(swipeRepo.save(any(Swipe.class))).thenAnswer(inv -> inv.getArgument(0));

        SwipeResponse response = service.recordSwipe(request(1L, 2L, SwipeDecision.PASS));

        assertFalse(response.matched());
        // crucially: we never even LOOK at the other side's swipe for a pass
        verify(swipeRepo, never()).findBySwiperIdAndSwipeeId(any(), any());
    }

    @Test
    @DisplayName("Passing side then liking later still matches (already-liked check)")
    void lateLikeMatches() {
        bothProfilesExist();
        when(swipeRepo.existsBySwiperIdAndSwipeeId(1L, 2L)).thenReturn(false);
        when(swipeRepo.findBySwiperIdAndSwipeeId(2L, 1L))
                .thenReturn(Optional.of(new Swipe(2L, 1L, SwipeDecision.SUPER_LIKE)));
        when(swipeRepo.save(any(Swipe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(matchRepo.findByUserAIdAndUserBId(1L, 2L)).thenReturn(Optional.empty());
        when(matchRepo.save(any(Match.class))).thenAnswer(inv -> inv.getArgument(0));

        SwipeResponse response = service.recordSwipe(request(1L, 2L, SwipeDecision.LIKE));

        assertTrue(response.matched());
    }

    @Test
    @DisplayName("Self-swipe is rejected")
    void selfSwipeRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.recordSwipe(request(1L, 1L, SwipeDecision.LIKE)));
    }

    @Test
    @DisplayName("Unknown profile -> 404 exception")
    void unknownProfileRejected() {
        when(profileRepo.existsById(1L)).thenReturn(true);
        when(profileRepo.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> service.recordSwipe(request(1L, 99L, SwipeDecision.LIKE)));
    }

    @Test
    @DisplayName("Duplicate swipe on same person is rejected")
    void duplicateSwipeRejected() {
        bothProfilesExist();
        when(swipeRepo.existsBySwiperIdAndSwipeeId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> service.recordSwipe(request(1L, 2L, SwipeDecision.LIKE)));
    }
}
