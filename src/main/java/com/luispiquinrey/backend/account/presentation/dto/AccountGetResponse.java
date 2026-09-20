package com.luispiquinrey.backend.account.presentation.dto;

import com.luispiquinrey.backend.account.application.dto.AccountView;

import java.time.Instant;

public record AccountGetResponse(
        String id,
        String email,
        String role,
        String status,
        boolean verified,
        Instant createdAt,
        Instant lastLoginAt
) {

    public static AccountGetResponse from(AccountView accountView) {
        return new AccountGetResponse(
                accountView.id(),
                accountView.email(),
                accountView.role(),
                accountView.status(),
                accountView.verified(),
                accountView.createdAt(),
                accountView.lastLoginAt()
        );
    }
}
