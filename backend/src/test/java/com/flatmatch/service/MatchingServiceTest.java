package com.flatmatch.service;

import com.flatmatch.dto.MatchResponse;
import com.flatmatch.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Plain unit tests — no Spring context, no database.
 * The scoring function is pure, so we test it directly and fast.
 */
class MatchingServiceTest {

    private final MatchingService service = new MatchingService(null, null); // repo unused in evaluate()

    private UserProfile profile(String name, int cleanliness, int social, int party,
                                Boolean nightOwl, int minBudget, int maxBudget,
                                Set<String> dealbreakers, Set<String> interests) {
        UserProfile p = new UserProfile();
        p.setName(name);
        p.setCleanliness(cleanliness);
        p.setSocialLevel(social);
        p.setPartyLevel(party);
        p.setNightOwl(nightOwl);
        p.setMinBudget(minBudget);
        p.setMaxBudget(maxBudget);
        p.setDealbreakers(dealbreakers);
        p.setInterests(interests);
        return p;
    }

    @Test
    @DisplayName("Identical lifestyles + shared interests -> capped at 100")
    void perfectMatch() {
        UserProfile me = profile("Me", 4, 3, 2, false, 8000, 15000, Set.of(), Set.of("trekking", "baking"));
        UserProfile twin = profile("Twin", 4, 3, 2, false, 8000, 15000, Set.of(), Set.of("trekking", "baking", "gaming"));

        MatchResponse result = service.evaluate(me, twin);

        assertNotNull(result);
        assertEquals(100, result.score()); // base 100 already, bonus capped at 10 -> 100
        assertEquals(2, result.sharedInterests().size());
    }

    @Test
    @DisplayName("Opposite sliders lose points in every dimension")
    void oppositeLifestyles() {
        // diff = 4 -> cleanliness 32, social 32, party 24; nightOwl mismatch -> 12 => 100-100 -> 0
        UserProfile me = profile("Me", 1, 1, 1, false, 8000, 15000, Set.of(), Set.of());
        UserProfile opposite = profile("Opp", 5, 5, 5, true, 8000, 15000, Set.of(), Set.of());

        MatchResponse result = service.evaluate(me, opposite);

        assertEquals(0, result.score());
        assertEquals(32, result.breakdown().get("cleanliness"));
        assertEquals(32, result.breakdown().get("social"));
        assertEquals(24, result.breakdown().get("party"));
        assertEquals(12, result.breakdown().get("schedule"));
    }

    @Test
    @DisplayName("Dealbreaker violation hard-filters the candidate (null)")
    void dealbreakerFilters() {
        UserProfile me = profile("Me", 4, 3, 2, false, 8000, 15000, Set.of("smoking"), Set.of());
        UserProfile smoker = profile("Smoker", 4, 3, 2, false, 8000, 15000, Set.of(), Set.of());
        smoker.setSmoker(true);

        assertNull(service.evaluate(me, smoker));
    }

    @Test
    @DisplayName("Non-overlapping budgets hard-filter the candidate")
    void budgetFilters() {
        UserProfile me = profile("Me", 3, 3, 3, null, 15000, 20000, Set.of(), Set.of());
        UserProfile cheap = profile("Cheap", 3, 3, 3, null, 5000, 9000, Set.of(), Set.of());

        assertNull(service.evaluate(me, cheap));
    }

    @Test
    @DisplayName("'loud-parties' dealbreaker filters partyLevel >= 4")
    void partyDealbreaker() {
        UserProfile me = profile("Me", 3, 3, 2, null, 8000, 15000, Set.of("loud-parties"), Set.of());
        UserProfile partyAnimal = profile("Party", 3, 3, 4, null, 8000, 15000, Set.of(), Set.of());
        UserProfile chill = profile("Chill", 3, 3, 3, null, 8000, 15000, Set.of(), Set.of());

        assertNull(service.evaluate(me, partyAnimal));
        assertNotNull(service.evaluate(me, chill));
    }

    @Test
    @DisplayName("Shared interests add a bonus of 3 points each")
    void interestBonus() {
        UserProfile me = profile("Me", 3, 3, 3, false, 8000, 15000, Set.of(), Set.of("gaming"));
        UserProfile noShared = profile("A", 3, 3, 3, true, 8000, 15000, Set.of(), Set.of("cooking"));
        UserProfile oneShared = profile("B", 3, 3, 3, true, 8000, 15000, Set.of(), Set.of("gaming"));

        assertEquals(100 - 12, service.evaluate(me, noShared).score());   // only nightOwl mismatch
        assertEquals(100 - 12 + 3, service.evaluate(me, oneShared).score());
    }

    @Test
    @DisplayName("findMatches ranks best first and skips excluded candidates")
    void ranking() {
        UserProfile me = profile("Me", 4, 3, 2, false, 8000, 15000, Set.of("smoking"), Set.of());
        me.setId(1L);

        UserProfile great = profile("Great", 4, 3, 2, false, 8000, 15000, Set.of(), Set.of("gaming"));
        great.setId(2L);
        UserProfile ok = profile("Ok", 2, 3, 2, false, 8000, 15000, Set.of(), Set.of()); // cleanliness diff 2 -> -16
        ok.setId(3L);
        UserProfile smoker = profile("Smoker", 4, 3, 2, false, 8000, 15000, Set.of(), Set.of());
        smoker.setId(4L);
        smoker.setSmoker(true);

        List<MatchResponse> matches = service.findMatches(me, List.of(smoker, ok, great, me));

        assertEquals(2, matches.size());
        assertEquals("Great", matches.get(0).name());
        assertEquals("Ok", matches.get(1).name());
    }
}
