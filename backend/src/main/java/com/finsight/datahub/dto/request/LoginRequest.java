package com.finsight.datahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 */
@Schema(description = "Login request with username and password")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Username", example = "admin")
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Password", example = "Admin@123")
    private String password;

    public LoginRequest() {}

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
