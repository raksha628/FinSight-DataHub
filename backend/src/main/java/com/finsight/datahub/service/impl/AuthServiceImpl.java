package com.finsight.datahub.service.impl;

import com.finsight.datahub.dto.request.LoginRequest;
import com.finsight.datahub.dto.request.RegisterRequest;
import com.finsight.datahub.dto.response.AuthResponse;
import com.finsight.datahub.dto.response.UserDTO;
import com.finsight.datahub.entity.User;
import com.finsight.datahub.exception.BadRequestException;
import com.finsight.datahub.exception.DuplicateResourceException;
import com.finsight.datahub.exception.ResourceNotFoundException;
import com.finsight.datahub.repository.UserRepository;
import com.finsight.datahub.security.JwtService;
import com.finsight.datahub.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service implementation.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        // Duplicate checks — fail fast before any DB writes
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException(
                    "Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException(
                    "Email '" + request.getEmail() + "' is already registered.");
        }

        // Parse and validate role
        User.Role role = User.Role.VIEWER; // Default
        if (request.getRole() != null && !request.getRole().isBlank()) {
            try {
                role = User.Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException(
                        "Invalid role: '" + request.getRole() +
                        "'. Valid roles are: ADMIN, ANALYST, VIEWER");
            }
        }

        // Build and persist the user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully — id={}, username={}, role={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

        // Issue JWT immediately so user doesn't need a separate login
        String token = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMs())
                .user(mapToUserDTO(savedUser))
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String password = request.getPassword() != null ? request.getPassword().trim() : "";
        log.info("Login attempt — username: '{}', raw password: '{}'", username, password);

        User user = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByUsername(username.toLowerCase()).orElse(null));

        if (user == null) {
            log.warn("Login failed: User '{}' not found in database.", username);
            throw new BadCredentialsException("Invalid username or password.");
        }

        log.info("User found — db username: '{}', db password hash: '{}'", user.getUsername(), user.getPassword());

        boolean matches = passwordEncoder.matches(password, user.getPassword());
        log.info("passwordEncoder.matches('{}', '{}') -> {}", password, user.getPassword(), matches);

        if (!matches) {
            log.warn("Login failed: Password mismatch for user '{}'", username);
            throw new BadCredentialsException("Invalid username or password.");
        }

        if (!user.isActive()) {
            log.warn("Login failed: User '{}' account is inactive", username);
            throw new BadCredentialsException("User account is inactive.");
        }

        try {
            String token = jwtService.generateToken(user);
            log.info("Login successful — username={}, role={}, tokenGenerated=true", user.getUsername(), user.getRole());

            return AuthResponse.builder()
                    .token(token)
                    .expiresIn(jwtService.getExpirationMs())
                    .user(mapToUserDTO(user))
                    .build();
        } catch (Exception e) {
            log.error("Error generating AuthResponse for user '{}': {}", username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        return mapToUserDTO(user);
    }

    private UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
