package com.luispiquinrey.backend.account.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

@ValueObject
public record EncodedPassword(String value) {

    public static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$.{56}$");

    public EncodedPassword {
        if (value == null) {
            throw new IllegalArgumentException("encoded password cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("encoded password cannot be blank");
        }
        if (!BCRYPT_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("encoded password must be BCrypt encoded");
        }
    }
}