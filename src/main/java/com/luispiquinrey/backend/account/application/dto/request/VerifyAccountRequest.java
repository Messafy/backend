package com.luispiquinrey.backend.account.application.dto.request;

public record VerifyAccountRequest(String token) {
    public VerifyAccountRequest {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("verification token cannot be blank");
        }
    }

    @Override
    public String toString() {
        return "VerifyAccountRequest[token=REDACTED]";
    }
}
