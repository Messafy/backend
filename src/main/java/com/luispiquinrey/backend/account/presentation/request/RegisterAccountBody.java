package com.luispiquinrey.backend.account.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registration data")
public record RegisterAccountBody(
        @Schema(example = "user@example.com") String email,
        @Schema(description = "Login password: at least 8 characters, at most 72 bytes, with a number and a symbol")
        String rawPassword,
        @Schema(description = "Locker password other users need to send you notes: 8-64 printable ASCII characters")
        String rawLockerPassword
) {
    @Override
    public String toString() {
        return "RegisterAccountBody[credentials=REDACTED]";
    }
}
