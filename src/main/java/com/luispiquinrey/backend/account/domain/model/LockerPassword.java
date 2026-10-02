package com.luispiquinrey.backend.account.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

@ValueObject
public record LockerPassword(String value) {

    public static final int MIN_RAW_LENGTH = 8;
    public static final int MAX_RAW_LENGTH = 64;

    private static final Pattern RAW_PATTERN = Pattern.compile("^[\\x21-\\x7E]+$");

    public LockerPassword {
        if (value == null) {
            throw new IllegalArgumentException("locker password encoded value cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("locker password encoded value cannot be blank");
        }
        if (!EncodedPassword.BCRYPT_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("locker password must be BCrypt encoded");
        }
    }

    public static void validateRaw(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("raw locker password cannot be null");
        }
        if (raw.length() < MIN_RAW_LENGTH) {
            throw new IllegalArgumentException(
                    "raw locker password must be at least %d characters".formatted(MIN_RAW_LENGTH)
            );
        }
        if (raw.length() > MAX_RAW_LENGTH) {
            throw new IllegalArgumentException(
                    "raw locker password cannot exceed %d characters".formatted(MAX_RAW_LENGTH)
            );
        }
        if (!RAW_PATTERN.matcher(raw).matches()) {
            throw new IllegalArgumentException(
                    "raw locker password must contain only printable ASCII characters and no whitespace"
            );
        }
    }
}