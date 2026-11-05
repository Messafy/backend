package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public record RegisterAccountRequest(
        String email,
        String rawPassword,
        String rawLockerPassword
) {
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_BYTES = 72;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).+$");

    public RegisterAccountRequest {
        email = new Email(email).email();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("raw password cannot be blank");
        }
        if (rawPassword.length() < MIN_PASSWORD_LENGTH
                || rawPassword.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            throw new IllegalArgumentException(
                    "raw password must contain at least %d characters and at most %d bytes"
                            .formatted(MIN_PASSWORD_LENGTH, MAX_PASSWORD_BYTES));
        }
        if (!PASSWORD_PATTERN.matcher(rawPassword).matches()) {
            throw new IllegalArgumentException("raw password must contain a number and a special character");
        }
        LockerPassword.validateRaw(rawLockerPassword);
    }

    public Email toEmail() {
        return new Email(email);
    }

    @Override
    public String toString() {
        return "RegisterAccountRequest[credentials=REDACTED]";
    }
}
