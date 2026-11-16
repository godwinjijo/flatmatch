package com.flatmatch.repository;

import com.flatmatch.model.Swipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SwipeRepository extends JpaRepository<Swipe, Long> {

    Optional<Swipe> findBySwiperIdAndSwipeeId(Long swiperId, Long swipeeId);

    boolean existsBySwiperIdAndSwipeeId(Long swiperId, Long swipeeId);

    /** Everyone I already decided about — used to filter them out of the deck. */
    List<Swipe> findBySwiperId(Long swiperId);
}
