package com.luispiquinrey.backend.account.presentation.dto;

import com.luispiquinrey.backend.account.application.dto.AccountView;

import java.time.Instant;

public record RegisterAccountResponse(
        String id,
        String email,
        String role,
        Instant createdAt
) {

    public static RegisterAccountResponse from(AccountView accountView) {
        return new RegisterAccountResponse(
                accountView.id(),
                accountView.email(),
                accountView.role(),
                accountView.createdAt()
        );
    }
}
