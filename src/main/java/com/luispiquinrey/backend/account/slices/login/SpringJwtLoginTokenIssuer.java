package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.infrastructure.AccountUserDetails;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class SpringJwtLoginTokenIssuer implements LoginTokenIssuer {

    private static final long TOKEN_EXPIRATION_MILLIS = 1000L * 60 * 60 * 24;

    private final SecretKey key;

    public SpringJwtLoginTokenIssuer(@Value("${security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String issue(AccountUserDetails accountUserDetails) {
        Account account = accountUserDetails.account();

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
