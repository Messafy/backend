package com.luispiquinrey.backend.account.infrastructure.security;

import io.jsonwebtoken.Claims;

enum TokenPurpose {
    ACCESS("access"),
    EMAIL_VERIFICATION("email-verification");

    static final String CLAIM = "typ";
    static final String EMAIL_CLAIM = "email";

    private final String value;

    TokenPurpose(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }

    boolean matches(Claims claims) {
        Object declared = claims.get(CLAIM);
        if (declared != null) {
            return value.equals(declared);
        }
        boolean hasEmail = claims.get(EMAIL_CLAIM) != null;
        return this == ACCESS ? hasEmail : !hasEmail;
    }
}
