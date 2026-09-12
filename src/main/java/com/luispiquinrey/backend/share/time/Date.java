package com.luispiquinrey.backend.share.time;

import org.jmolecules.ddd.annotation.ValueObject;

import java.time.Instant;

@ValueObject
public record Date(Instant value) {

    public Date {
        if (value == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
    }

    public static Date now() {
        return new Date(Instant.now());
    }
}
