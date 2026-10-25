package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;

import java.util.regex.Pattern;

public record RegisterAccountRequest(
        String email,
        String rawPassword,
        String rawLockerPassword
) {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s]).+$");

    public RegisterAccountRequest {
        email = new Email(email).email();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("raw password cannot be blank");
        }
        if (rawPassword.length() < 8 || rawPassword.length() > 128) {
            throw new IllegalArgumentException("raw password must contain between 8 and 128 characters");
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
