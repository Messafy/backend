package com.luispiquinrey.backend.account.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login credentials")
public record LoginAccountBody(
        @Schema(example = "user@example.com") String email,
        String rawPassword
) {
    @Override
    public String toString() {
        return "LoginAccountBody[credentials=REDACTED]";
    }
}
