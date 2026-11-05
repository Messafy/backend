package com.luispiquinrey.backend.account.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class SpringJwtTokenVerifier implements JwtTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(SpringJwtTokenVerifier.class);

    private final SecretKey key;

    public SpringJwtTokenVerifier(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Optional<String> verifyAccessToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (claims.getExpiration() == null || !TokenPurpose.ACCESS.matches(claims)) {
                log.debug("JWT rejected because it has no expiration or is not an access token");
                return Optional.empty();
            }
            String email = claims.get(TokenPurpose.EMAIL_CLAIM, String.class);
            return email == null || email.isBlank() ? Optional.empty() : Optional.of(email);
        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT validation failed");
            return Optional.empty();
        }
    }
}
