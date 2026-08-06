package com.finsight.datahub.controller;

import com.finsight.datahub.dto.request.LoginRequest;
import com.finsight.datahub.dto.request.RegisterRequest;
import com.finsight.datahub.dto.response.ApiResponse;
import com.finsight.datahub.dto.response.AuthResponse;
import com.finsight.datahub.dto.response.UserDTO;
import com.finsight.datahub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and profile endpoints")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── POST /api/auth/register ─────────────────────────────────────────────

    /**
     * Registers a new user account and returns a JWT token immediately.
     * The user does not need to login separately after registration.
     *
     * @param request validated registration payload
     * @return 201 Created with JWT token and user profile
     */
    @PostMapping("/register")
    @Operation(
        summary     = "Register a new user",
        description = "Creates a new user account. Roles: ADMIN, ANALYST (default: VIEWER). " +
                      "Returns a JWT token immediately upon successful registration."
    )
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        log.info("Registration request — username: {}", request.getUsername());
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful. Welcome to FinSight DataHub!", authResponse));
    }

    // ── POST /api/auth/login ────────────────────────────────────────────────

    /**
     * Authenticates a user and returns a JWT token.
     * The token should be included in subsequent requests as:
     * {@code Authorization: Bearer <token>}
     *
     * @param request login credentials
     * @return 200 OK with JWT token and user profile
     */
    @PostMapping("/login")
    @Operation(
        summary     = "Login and receive JWT token",
        description = "Authenticates with username/password. Returns a JWT valid for 24 hours. " +
                      "Default credentials — Admin: admin/Admin@123 | Analyst: analyst/Analyst@123"
    )
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        log.info("Login request — username: {}", request.getUsername());
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful.", authResponse));
    }

    // ── GET /api/auth/me ────────────────────────────────────────────────────

    /**
     * Returns the profile of the currently authenticated user.
     * Uses {@code @AuthenticationPrincipal} to extract the user
     * from the Spring Security context — no manual JWT parsing required.
     *
     * @param userDetails injected from the SecurityContext by Spring Security
     * @return 200 OK with the user profile
     */
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
        summary     = "Get current user profile",
        description = "Returns the authenticated user's profile. Requires a valid JWT token."
    )
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        UserDTO user = authService.getCurrentUser(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved.", user));
    }
}
