package com.luispiquinrey.backend.account.application.dto.response;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.share.time.Date;

import java.time.Instant;

public record AccountResponse(
        String id,
        String email,
        String publicCode,
        String role,
        String status,
        boolean verified,
        Instant createdAt,
        Instant lastLoginAt
) {

    public static AccountResponse from(Account account) {
        Date createdAt = account.createdAt();
        Date lastLoginAt = account.lastLoginAt();
        return new AccountResponse(
                account.id().id(),
                account.email().email(),
                account.publicCode().value(),
                account.role().name(),
                account.status().name(),
                account.isVerified(),
                createdAt == null ? null : createdAt.value(),
                lastLoginAt == null ? null : lastLoginAt.value()
        );
    }
}
