package com.luispiquinrey.backend.account.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

@ValueObject
public record PublicCode(String value) {

    public static final int LENGTH = 12;

    private static final Pattern pattern = Pattern.compile("^[A-Za-z0-9_-]{" + LENGTH + "}$");

    public PublicCode {
        if (value == null) {
            throw new IllegalArgumentException("public code cannot be null");
        }
        if (value.isBlank()) {
            throw new IllegalArgumentException("public code cannot be blank");
        }
        if (value.length() != LENGTH) {
            throw new IllegalArgumentException(
                    "public code must be exactly %d characters".formatted(LENGTH)
            );
        }
        if (!pattern.matcher(value).matches()) {
            throw new IllegalArgumentException(
                    "public code must contain only NanoId characters [A-Za-z0-9_-]"
            );
        }
    }
}