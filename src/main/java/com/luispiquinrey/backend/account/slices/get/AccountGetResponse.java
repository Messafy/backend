package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.share.time.Date;

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

    public static AccountGetResponse from(Account account) {
        Date createdAt = account.createdAt();
        Date lastLoginAt = account.lastLoginAt();
        return new AccountGetResponse(
                account.id().id(),
                account.email().email(),
                account.role().name(),
                account.status().name(),
                account.isVerified(),
                createdAt == null ? null : createdAt.value(),
                lastLoginAt == null ? null : lastLoginAt.value()
        );
    }
}
