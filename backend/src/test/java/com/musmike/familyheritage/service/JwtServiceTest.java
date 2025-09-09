package com.musmike.familyheritage.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private final String testSecret = "VGhpcyBpcyBhIHNlY3VyZSB0ZXN0IGtleSBmb3IgSldUIGVuY29kaW5nIGFuZCBtdXN0IGJlIGxvbmc=";
    private final long testExpiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", testExpiration);

        jwtService.init();
    }

    @Test
    void shouldGenerateTokenWithCorrectUsernameAndExpiration() {
        // GIVEN
        UserDetails userDetails = new User("testuser", "password", Collections.emptyList());

        // WHEN
        String token = jwtService.generateToken(userDetails);

        // THEN
        assertNotNull(token);

        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("testuser", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
        assertTrue(claims.getExpiration().getTime() - claims.getIssuedAt().getTime() <= testExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(testSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}