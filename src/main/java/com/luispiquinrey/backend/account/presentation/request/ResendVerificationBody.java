package com.luispiquinrey.backend.account.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Email address of the account that needs a new verification email")
public record ResendVerificationBody(@Schema(example = "user@example.com") String email) {
}
