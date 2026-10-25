package com.luispiquinrey.backend.notify.application.dto;

public record SendVerificationEmailRequest(String accountId, String email) {

    public SendVerificationEmailRequest {
        requireNonBlank(accountId, "account id");
        requireNonBlank(email, "email");
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be blank");
        }
    }
}
