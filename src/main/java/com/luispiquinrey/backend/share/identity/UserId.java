package com.luispiquinrey.backend.share.identity;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

@ValueObject
public record UserId(String id) {

    private static final Pattern PATTERN = Pattern.compile("^[a-fA-F0-9]{24}$");

    public UserId {
        switch (id) {
            case null -> throw new IllegalArgumentException("id cannot be null");
            case "" -> throw new IllegalArgumentException("id cannot be empty");
            default -> {
                if (!PATTERN.matcher(id).matches()) {
                    throw new IllegalArgumentException("id must be a valid MongoDB ObjectId");
                }
            }
        }
    }
}
