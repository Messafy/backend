package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.account.domain.model.Email;

public record LoginAccountRequest(
        String email,
        String rawPassword
) {
    public LoginAccountRequest {
        email = new Email(email).email();
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("raw password cannot be blank");
        }
        if (rawPassword.length() > 128) {
            throw new IllegalArgumentException("raw password cannot exceed 128 characters");
        }
    }

    public Email toEmail() {
        return new Email(email);
    }

    @Override
    public String toString() {
        return "LoginAccountRequest[credentials=REDACTED]";
    }
}
