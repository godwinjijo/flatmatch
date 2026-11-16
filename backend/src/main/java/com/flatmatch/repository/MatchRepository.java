package com.flatmatch.repository;

import com.flatmatch.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByUserAIdAndUserBId(Long userAId, Long userBId);

    boolean existsByUserAIdAndUserBId(Long userAId, Long userBId);

    /** All matches where userId is either participant (Spring derives the OR). */
    List<Match> findByUserAIdOrUserBId(Long userAId, Long userBId);
}
