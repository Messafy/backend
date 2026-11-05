package com.luispiquinrey.backend.account.application.dto.response;

public record ResendVerificationResponse(boolean accepted) {
    public static ResendVerificationResponse acceptedResponse() {
        return new ResendVerificationResponse(true);
    }
}
