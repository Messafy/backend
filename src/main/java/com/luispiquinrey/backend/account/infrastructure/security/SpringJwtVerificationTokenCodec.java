package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SpringJwtVerificationTokenCodec implements VerificationTokenCodec {

    private static final Logger log = LoggerFactory.getLogger(SpringJwtVerificationTokenCodec.class);

    private static final long VERIFICATION_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24;

    private final SecretKey key;

    public SpringJwtVerificationTokenCodec(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(String accountId) {
        log.debug("Issuing verification token for account {}", accountId);
        return Jwts.builder()
                .subject(accountId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + VERIFICATION_TOKEN_EXPIRATION_MILLIS))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    @Override
    public String extractAccountId(String rawToken) {
        try {
            String accountId = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload()
                    .getSubject();
            log.debug("Extracted account {} from verification token", accountId);
            return accountId;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Verification token is invalid or expired: {}", e.getMessage());
            throw new AccountVerificationTokenException("verification token is invalid or expired");
        }
    }
}
