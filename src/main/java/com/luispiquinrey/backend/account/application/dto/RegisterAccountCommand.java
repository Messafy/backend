package com.luispiquinrey.backend.account.application.dto;

public record RegisterAccountCommand(
        String email,
        String rawPassword
) {
}
