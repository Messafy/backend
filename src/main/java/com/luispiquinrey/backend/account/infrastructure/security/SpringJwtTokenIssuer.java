package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.application.port.TokenIssuer;
import com.luispiquinrey.backend.account.domain.model.Account;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class SpringJwtTokenIssuer implements TokenIssuer {

    private static final Logger log = LoggerFactory.getLogger(SpringJwtTokenIssuer.class);

    private static final long TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24;

    private final SecretKey key;

    public SpringJwtTokenIssuer(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(Account account) {
        log.debug("Issuing JWT for account {}", account.id().id());

        return Jwts.builder()
                .subject(account.id().id())
                .claim("email", account.email().email())
                .claim("role", account.role().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MILLIS))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
