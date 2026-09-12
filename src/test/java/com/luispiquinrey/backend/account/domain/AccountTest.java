package com.luispiquinrey.backend.account.domain;

import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

public class AccountTest {
    private static final UserId ID = new UserId("507f1f77bcf86cd799439011");
    private static final Email EMAIL = new Email("user@example.com");
    private static final EncodedPassword PASSWORD = new EncodedPassword("encoded-password");

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireId() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .email(EMAIL)
                .encodedPassword(PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireEmail() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .encodedPassword(PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireEncodedPassword() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldCreateAccountWithDefaults() {
        Account account = validAccountBuilder().build();

        Assertions.assertAll(
                () -> Assertions.assertEquals(Role.USER, account.role()),
                () -> Assertions.assertEquals(AccountStatus.ACTIVE, account.status()),
                () -> Assertions.assertTrue(account.isActive()),
                () -> Assertions.assertFalse(account.isVerified()),
                () -> Assertions.assertNotNull(account.createdAt()),
                () -> Assertions.assertNull(account.lastLoginAt())
        );
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldPreserveProvidedValues() {
        Date createdAt = new Date(Instant.parse("2026-09-01T10:00:00Z"));
        Date lastLoginAt = new Date(Instant.parse("2026-09-02T10:00:00Z"));

        Account account = validAccountBuilder()
                .role(Role.ADMIN)
                .status(AccountStatus.DELETED)
                .isVerified(true)
                .createdAt(createdAt)
                .lastLoginAt(lastLoginAt)
                .build();

        Assertions.assertAll(
                () -> Assertions.assertEquals(ID, account.id()),
                () -> Assertions.assertEquals(EMAIL, account.email()),
                () -> Assertions.assertEquals(PASSWORD, account.encodedPassword()),
                () -> Assertions.assertEquals(Role.ADMIN, account.role()),
                () -> Assertions.assertEquals(AccountStatus.DELETED, account.status()),
                () -> Assertions.assertTrue(account.isVerified()),
                () -> Assertions.assertEquals(createdAt, account.createdAt()),
                () -> Assertions.assertEquals(lastLoginAt, account.lastLoginAt())
        );
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldVerifyActiveAccount() {
        Account account = validAccountBuilder().build();

        account.verify();

        Assertions.assertTrue(account.isVerified());
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRejectVerificationForDeletedAccount() {
        Account account = validAccountBuilder()
                .status(AccountStatus.DELETED)
                .build();

        Assertions.assertThrows(AccountConflictException.class, account::verify);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRecordLoginForActiveAccount() {
        Account account = validAccountBuilder().build();

        account.login();

        Assertions.assertNotNull(account.lastLoginAt());
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRejectLoginForDeletedAccount() {
        Account account = validAccountBuilder()
                .status(AccountStatus.DELETED)
                .build();

        Assertions.assertThrows(AccountConflictException.class, account::login);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldDeleteActiveAccount() {
        Account account = validAccountBuilder().build();

        account.delete();

        Assertions.assertAll(
                () -> Assertions.assertEquals(AccountStatus.DELETED, account.status()),
                () -> Assertions.assertFalse(account.isActive()),
                () -> Assertions.assertTrue(account.isFinal())
        );
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldAllowDeletingAccountMoreThanOnce() {
        Account account = validAccountBuilder().build();
        account.delete();

        Assertions.assertDoesNotThrow(account::delete);
        Assertions.assertEquals(AccountStatus.DELETED, account.status());
    }

    private Account.AccountBuilder validAccountBuilder() {
        return new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL)
                .encodedPassword(PASSWORD);
    }
}
