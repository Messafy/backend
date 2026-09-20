package com.luispiquinrey.backend.account.infrastructure.security;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenVerifier {

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
