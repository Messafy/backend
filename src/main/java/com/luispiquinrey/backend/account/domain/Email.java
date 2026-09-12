package com.luispiquinrey.backend.account.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import java.util.regex.Pattern;

@ValueObject
public record Email(String email) {

    private static final Pattern pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    public Email {
        if (email == null) {
            throw new IllegalArgumentException("email cannot be null");
        }

        email = email.toLowerCase();
        switch (email) {
            case "" -> throw new IllegalArgumentException("email cannot be empty");
            default -> {
                if (!pattern.matcher(email).matches()) {
                    throw new IllegalArgumentException("email must be a valid email address");
                }
            }
        }
    }
}
