package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringJwtTokenVerifierTest {

    private static final String SECRET = "test-secret-key-with-at-least-thirty-two-characters";
    private static final String OTHER_SECRET = "another-test-key-with-at-least-thirty-two-characters";
    private static final String EMAIL = "user@example.com";

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void shouldRejectExpiredTokens() {
        SpringJwtTokenVerifier verifier = new SpringJwtTokenVerifier(SECRET);
        UserDetails user = User.withUsername(EMAIL).password("password").roles("USER").build();
        String expiredToken = Jwts.builder()
                .claim("email", EMAIL)
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertFalse(verifier.isTokenValid(expiredToken, user));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void shouldValidateTokensIssuedForTheirUser() {
        Account account = account();
        String token = new SpringJwtTokenIssuer(SECRET).issue(account);
        SpringJwtTokenVerifier verifier = new SpringJwtTokenVerifier(SECRET);

        assertEquals(EMAIL, verifier.extractEmail(token));
        assertTrue(verifier.isTokenValid(token, new AccountUserDetails(account)));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void shouldRejectTokensSignedWithAnotherKey() {
        UserDetails user = User.withUsername(EMAIL).password("password").roles("USER").build();
        String token = Jwts.builder()
                .claim("email", EMAIL)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertFalse(new SpringJwtTokenVerifier(SECRET).isTokenValid(token, user));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void shouldRejectTokensWithoutAnEmailClaim() {
        UserDetails user = User.withUsername(EMAIL).password("password").roles("USER").build();
        String token = Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertFalse(new SpringJwtTokenVerifier(SECRET).isTokenValid(token, user));
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new com.luispiquinrey.backend.share.time.Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new com.luispiquinrey.backend.share.time.Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();
    }
}
