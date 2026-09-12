package com.luispiquinrey.backend.account.domain;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record EncodedPassword(String value) {
    public EncodedPassword {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("encoded password cannot be blank");
        }
    }
}
