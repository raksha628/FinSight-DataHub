package com.finsight.datahub.security;

import com.finsight.datahub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring Security {@link UserDetailsService} implementation that loads
 * users from the PostgreSQL database.
 *
 * <p>Called automatically by the Spring Security authentication manager
 * during login and by {@link JwtAuthFilter} on every authenticated request.</p>
 *
 * <p>The {@link com.finsight.datahub.entity.User} entity implements
 * {@link UserDetails} directly, so no wrapper is needed.</p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by username for Spring Security authentication.
     *
     * @param username the username from the login request
     * @return the UserDetails (our User entity) for authentication
     * @throws UsernameNotFoundException if no user exists with this username
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found — username={}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
    }
}
