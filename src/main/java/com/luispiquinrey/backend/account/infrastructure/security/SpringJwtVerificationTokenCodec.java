package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SpringJwtVerificationTokenCodec implements VerificationTokenCodec {

    private static final long VERIFICATION_TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24;

    private final SecretKey key;

    public SpringJwtVerificationTokenCodec(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(String accountId) {
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
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new AccountVerificationTokenException("verification token is invalid or expired");
        }
    }
}
