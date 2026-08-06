package com.finsight.datahub.config;

import com.finsight.datahub.security.AuthEntryPoint;
import com.finsight.datahub.security.CustomUserDetailsService;
import com.finsight.datahub.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration for FinSight DataHub.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter              jwtAuthFilter;
    private final CustomUserDetailsService   userDetailsService;
    private final AuthEntryPoint             authEntryPoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CustomUserDetailsService userDetailsService, AuthEntryPoint authEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
    }

    /**
     * Main security filter chain defining authorization rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ── Disable CSRF (stateless API — no session cookies) ────────────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Authorization Rules ─────────────────────────────────────────────
                .authorizeHttpRequests(auth -> auth

                    // Public endpoints — no authentication required
                    .requestMatchers("/api/auth/**").permitAll()

                    // Swagger / OpenAPI documentation
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/v3/api-docs/**"
                    ).permitAll()

                    // Actuator health endpoint
                    .requestMatchers("/actuator/health").permitAll()

                    // Static React frontend — served from classpath:/static/
                    .requestMatchers(
                        "/",
                        "/index.html",
                        "/static/**",
                        "/assets/**",
                        "/favicon.ico",
                        "/manifest.json"
                    ).permitAll()

                    // Admin-only endpoints
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // Upload and AI queries — ANALYST or ADMIN
                    .requestMatchers(HttpMethod.POST, "/api/upload/**").hasAnyRole("ANALYST", "ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/ai/**").hasAnyRole("ANALYST", "ADMIN")

                    // All other /api/** requests require authentication
                    .requestMatchers("/api/**").authenticated()

                    // All other requests (React SPA routes) are public
                    .anyRequest().permitAll()
                )

                // ── Stateless Session ───────────────────────────────────────────────
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ── Authentication Provider ─────────────────────────────────────────
                .authenticationProvider(authenticationProvider())

                // ── JWT Filter (before Spring's UsernamePasswordAuthenticationFilter) ─
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                // ── Exception Handling ──────────────────────────────────────────────
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authEntryPoint)
                )

                .build();
    }

    /**
     * DaoAuthenticationProvider wires together our UserDetailsService
     * and PasswordEncoder for standard username/password authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean for use in AuthServiceImpl.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder at strength 12.
     *
     * <p>Strength 12 adds approximately 4x computational cost vs. the default
     * strength 10, making brute-force attacks significantly harder.
     * Each hash operation takes ~300ms on modern hardware — acceptable for
     * login flows but not for hot paths.</p>
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
