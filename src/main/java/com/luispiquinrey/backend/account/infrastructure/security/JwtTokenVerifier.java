package com.luispiquinrey.backend.account.infrastructure.security;

import java.util.Optional;

public interface JwtTokenVerifier {

    Optional<String> verifyAccessToken(String token);
}
