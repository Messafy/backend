package com.luispiquinrey.backend.account.infrastructure;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtTokenVerifier {

    String extractEmail(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
