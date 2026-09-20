package com.luispiquinrey.backend.account.application.dto;

public record LoginAccountCommand(
        String email,
        String rawPassword
) {
}
