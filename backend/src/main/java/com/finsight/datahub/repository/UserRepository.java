package com.finsight.datahub.repository;

import com.finsight.datahub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for {@link User} entity.
 *
 * <p>Spring Data JPA generates the implementation at runtime.
 * All queries are type-safe and avoid N+1 issues.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     * Used by {@link com.finsight.datahub.security.CustomUserDetailsService}
     * during authentication.
     *
     * @param username the login username
     * @return the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their email address.
     * Used during registration to check for duplicates.
     *
     * @param email the user's email
     * @return the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a username is already taken.
     * More efficient than findByUsername for existence checks (no entity load).
     *
     * @param username the username to check
     * @return true if the username already exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email address is already registered.
     *
     * @param email the email to check
     * @return true if the email already exists
     */
    boolean existsByEmail(String email);
}
