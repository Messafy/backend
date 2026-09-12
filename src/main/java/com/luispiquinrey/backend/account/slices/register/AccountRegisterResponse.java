package com.luispiquinrey.backend.account.slices.register;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.share.time.Date;

import java.time.Instant;

public record AccountRegisterResponse(
        String id,
        String email,
        String role,
        Instant createdAt
) {

    public static AccountRegisterResponse from(Account account) {
        Date createdAt = account.createdAt();
        return new AccountRegisterResponse(
                account.id().id(),
                account.email().email(),
                account.role().name(),
                createdAt == null ? null : createdAt.value()
        );
    }
}
