package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringJwtTokenVerifierTest {

    private static final String SECRET = "test-secret-key-with-at-least-thirty-two-characters";
    private static final String OTHER_SECRET = "another-test-key-with-at-least-thirty-two-characters";
    private static final String EMAIL = "user@example.com";
    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";

    private final SpringJwtTokenVerifier verifier = new SpringJwtTokenVerifier(SECRET);

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void acceptsAccessTokensIssuedByTheApplication() {
        String token = new SpringJwtTokenIssuer(SECRET).issue(account());

        assertEquals(Optional.of(EMAIL), verifier.verifyAccessToken(token));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void acceptsLegacyAccessTokensWithoutAPurposeClaim() {
        String token = signed(SECRET).claim("email", EMAIL).subject(ACCOUNT_ID).compact();

        assertEquals(Optional.of(EMAIL), verifier.verifyAccessToken(token));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsVerificationTokensUsedAsAccessTokens() {
        String verification = new SpringJwtVerificationTokenCodec(SECRET).issue(ACCOUNT_ID);
        String forged = signed(SECRET).claim("typ", "email-verification").claim("email", EMAIL).compact();

        assertTrue(verifier.verifyAccessToken(verification).isEmpty());
        assertTrue(verifier.verifyAccessToken(forged).isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsAccessTokensUsedAsVerificationTokens() {
        String access = new SpringJwtTokenIssuer(SECRET).issue(account());
        SpringJwtVerificationTokenCodec codec = new SpringJwtVerificationTokenCodec(SECRET);

        assertThrows(AccountVerificationTokenException.class, () -> codec.extractAccountId(access));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void acceptsVerificationTokensWithAndWithoutAPurposeClaim() {
        SpringJwtVerificationTokenCodec codec = new SpringJwtVerificationTokenCodec(SECRET);
        String legacy = signed(SECRET).subject(ACCOUNT_ID).compact();

        assertEquals(ACCOUNT_ID, codec.extractAccountId(codec.issue(ACCOUNT_ID)));
        assertEquals(ACCOUNT_ID, codec.extractAccountId(legacy));
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsExpiredTokens() {
        String expired = Jwts.builder()
                .claim("typ", "access")
                .claim("email", EMAIL)
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256)
                .compact();

        assertTrue(verifier.verifyAccessToken(expired).isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsTokensSignedWithAnotherKey() {
        String token = signed(OTHER_SECRET).claim("typ", "access").claim("email", EMAIL).compact();

        assertTrue(verifier.verifyAccessToken(token).isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsTokensWithoutAnEmailClaim() {
        String token = signed(SECRET).claim("typ", "access").compact();

        assertTrue(verifier.verifyAccessToken(token).isEmpty());
    }

    @Test
    @Timeout(1)
    @Tag("springJwtTokenVerifier")
    void rejectsTokensWithoutExpirationAndMalformedTokens() {
        String withoutExpiration = Jwts.builder().claim("typ", "access").claim("email", EMAIL)
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256).compact();

        assertTrue(verifier.verifyAccessToken(withoutExpiration).isEmpty());
        assertTrue(verifier.verifyAccessToken("malformed").isEmpty());
        assertTrue(verifier.verifyAccessToken("").isEmpty());
        assertTrue(verifier.verifyAccessToken(null).isEmpty());
    }

    private static JwtBuilder signed(String secret) {
        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), Jwts.SIG.HS256);
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
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
