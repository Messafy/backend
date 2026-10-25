package com.luispiquinrey.backend.account.presentation.viewmodel;

public record RegisteredAccountViewModel(
        String id,
        String email,
        String publicCode,
        String role,
        String createdAt
) {
}
