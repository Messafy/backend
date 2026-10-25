package com.luispiquinrey.backend.account.presentation.viewmodel;

public record AccountViewModel(
        String id,
        String email,
        String publicCode,
        String role,
        String status,
        boolean verified,
        String createdAt,
        String lastLoginAt
) {
}
