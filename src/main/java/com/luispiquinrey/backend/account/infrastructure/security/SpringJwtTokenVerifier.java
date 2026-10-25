package com.luispiquinrey.backend.account.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class SpringJwtTokenVerifier implements JwtTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(SpringJwtTokenVerifier.class);

    private final SecretKey key;

    public SpringJwtTokenVerifier(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        try {
            new AccountStatusUserDetailsChecker().check(userDetails);
            return extractClaim(token, claims -> {
                Date expiration = claims.getExpiration();
                return userDetails.getUsername().equals(claims.get("email", String.class))
                        && expiration != null && expiration.after(new Date());
            });
        } catch (JwtException | IllegalArgumentException | AuthenticationException exception) {
            log.debug("JWT validation failed");
            return false;
        }
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
