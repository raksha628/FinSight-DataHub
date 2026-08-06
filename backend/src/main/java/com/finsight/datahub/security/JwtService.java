package com.finsight.datahub.security;

import com.finsight.datahub.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service responsible for JWT token lifecycle.
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ── Token Generation ────────────────────────────────────────────────────

    /**
     * Generates a JWT token for the given user with standard claims.
     *
     * @param user the authenticated user
     * @return signed JWT string
     */
    public String generateToken(User user) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", user.getId());
        extraClaims.put("role",   user.getRole().name());
        extraClaims.put("email",  user.getEmail());
        return buildToken(extraClaims, user, expirationMs);
    }

    /**
     * Builds and signs the JWT token.
     *
     * @param extraClaims additional claims to embed in the token
     * @param userDetails the principal
     * @param expiration  token lifetime in milliseconds
     * @return the signed JWT string
     */
    private String buildToken(Map<String, Object> extraClaims,
                              UserDetails userDetails,
                              long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // ── Token Validation ────────────────────────────────────────────────────

    /**
     * Validates the token: checks signature, expiry, and username match.
     *
     * @param token       the JWT string from the Authorization header
     * @param userDetails the user to validate against
     * @return true if the token is valid for this user
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claims Extraction ───────────────────────────────────────────────────

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT string
     * @return the username stored as the JWT subject
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the user ID from the JWT custom claims.
     *
     * @param token the JWT string
     * @return the user's database ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims ->
            ((Number) claims.get("userId")).longValue()
        );
    }

    /**
     * Extracts the user's role from the JWT custom claims.
     *
     * @param token the JWT string
     * @return the role string (e.g. "ADMIN", "ANALYST", "VIEWER")
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> (String) claims.get("role"));
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic claims extractor using a functional resolver.
     *
     * @param token          the JWT string
     * @param claimsResolver a function to apply on the claims object
     * @param <T>            the return type
     * @return the resolved value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and verifies the JWT signature, returning all claims.
     * Throws {@link JwtException} if the token is invalid or expired.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Derives the HMAC-SHA256 signing key from the configured secret.
     * The secret is treated as Base64-encoded bytes.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Returns the token expiration duration in milliseconds.
     * Useful for setting response headers or cookies.
     */
    public long getExpirationMs() {
        return expirationMs;
    }
}
