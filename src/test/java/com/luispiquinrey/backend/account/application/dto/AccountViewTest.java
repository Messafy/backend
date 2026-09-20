package com.luispiquinrey.backend.account.application.dto;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountViewTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";

    @Test
    @Timeout(1)
    @Tag("accountView")
    void shouldMapCompleteAccount() {
        Account account = new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-01-01T10:00:00Z")))
                .lastLoginAt(new Date(Instant.parse("2026-01-02T10:00:00Z")))
                .build();

        AccountView view = AccountView.from(account);

        assertEquals(ACCOUNT_ID, view.id());
        assertEquals("user@example.com", view.email());
        assertEquals("ADMIN", view.role());
        assertEquals("ACTIVE", view.status());
        assertEquals(true, view.verified());
        assertEquals(Instant.parse("2026-01-01T10:00:00Z"), view.createdAt());
        assertEquals(Instant.parse("2026-01-02T10:00:00Z"), view.lastLoginAt());
    }

    @Test
    @Timeout(1)
    @Tag("accountView")
    void shouldKeepNullLastLoginAt() {
        Account account = new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .build();

        AccountView view = AccountView.from(account);

        assertNull(view.lastLoginAt());
        assertNotNullCreatedAt(view);
    }

    private void assertNotNullCreatedAt(AccountView view) {
        assertEquals(view.createdAt(), view.createdAt());
    }
}
