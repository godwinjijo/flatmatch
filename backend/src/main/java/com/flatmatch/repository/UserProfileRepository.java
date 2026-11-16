package com.flatmatch.repository;

import com.flatmatch.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA creates the implementation automatically.
 * findByEmail works just by naming the method correctly — Spring builds
 * "SELECT * FROM user_profiles WHERE email = ?" from the name.
 */
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    Optional<UserProfile> findByAuthUserId(Long authUserId);
}
