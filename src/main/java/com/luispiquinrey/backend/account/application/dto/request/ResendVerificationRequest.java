package com.luispiquinrey.backend.account.application.dto.request;

import com.luispiquinrey.backend.account.domain.model.Email;

public record ResendVerificationRequest(String email) {
    public ResendVerificationRequest {
        email = new Email(email).email();
    }
}
