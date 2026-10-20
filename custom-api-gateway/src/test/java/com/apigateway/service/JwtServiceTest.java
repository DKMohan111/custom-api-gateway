
package com.apigateway.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET =
            "test-secret-for-jwt-unit-tests-32bytes";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void generateToken_containsUsernameAndRole() {
        String token = jwtService.generateToken("admin", "ADMIN");

        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void extractUsername_rejectsTokenSignedWithDifferentSecret() {
        String otherSecret =
                "different-test-secret-for-jwt-32bytes";

        SecretKey otherKey = Keys.hmacShaKeyFor(
                otherSecret.getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
                .subject("admin")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername(token)
        );
    }

    @Test
    void extractUsername_rejectsExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );

        Date now = new Date();
        Date expired = new Date(now.getTime() - 60_000);

        String token = Jwts.builder()
                .subject("admin")
                .claim("role", "ADMIN")
                .issuedAt(new Date(now.getTime() - 120_000))
                .expiration(expired)
                .signWith(key)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername(token)
        );
    }

    @Test
    void extractUsername_rejectsMalformedToken() {
        assertThrows(
                JwtException.class,
                () -> jwtService.extractUsername("not-a-valid-jwt")
        );
    }
}
