package com.finsight.datahub.service;

import com.finsight.datahub.dto.request.LoginRequest;
import com.finsight.datahub.dto.request.RegisterRequest;
import com.finsight.datahub.dto.response.AuthResponse;
import com.finsight.datahub.dto.response.UserDTO;
import com.finsight.datahub.entity.User;

/**
 * Authentication service contract.
 *
 * <p>Defines the authentication operations exposed by the auth module.
 * The implementation ({@link com.finsight.datahub.service.impl.AuthServiceImpl})
 * contains all business logic — controllers remain thin wrappers.</p>
 *
 * <p>Coding to an interface allows easy mocking in unit tests
 * and alternative implementations (e.g., OAuth2 provider swap).</p>
 */
public interface AuthService {

    /**
     * Registers a new user and returns a JWT token immediately
     * (so the user doesn't have to login after registering).
     *
     * @param request the registration payload
     * @return JWT auth response with token and user profile
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user by username and password.
     *
     * @param request login credentials
     * @return JWT auth response with token and user profile
     */
    AuthResponse login(LoginRequest request);

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param username extracted from the JWT token by the controller
     * @return the user's profile DTO
     */
    UserDTO getCurrentUser(String username);
}
