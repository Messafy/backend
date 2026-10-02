package com.luispiquinrey.backend.account.domain.model;

import com.luispiquinrey.backend.account.domain.exception.AccountConflictException;
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
    private static final EncodedPassword PASSWORD = new EncodedPassword("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
    private static final PublicCode PUBLIC_CODE = new PublicCode("V1StGXR8_Z5j");
    private static final LockerPassword LOCKER_PASSWORD = new LockerPassword("$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e");

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireId() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .email(EMAIL)
                .encodedPassword(PASSWORD)
                .publicCode(PUBLIC_CODE)
                .lockerPassword(LOCKER_PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireEmail() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .encodedPassword(PASSWORD)
                .publicCode(PUBLIC_CODE)
                .lockerPassword(LOCKER_PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireEncodedPassword() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL)
                .publicCode(PUBLIC_CODE)
                .lockerPassword(LOCKER_PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequirePublicCode() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL)
                .encodedPassword(PASSWORD)
                .lockerPassword(LOCKER_PASSWORD);

        Assertions.assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRequireLockerPassword() {
        Account.AccountBuilder builder = new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL)
                .encodedPassword(PASSWORD)
                .publicCode(PUBLIC_CODE);

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
                () -> Assertions.assertEquals(PUBLIC_CODE, account.publicCode()),
                () -> Assertions.assertEquals(LOCKER_PASSWORD, account.lockerPassword()),
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
        PublicCode customPublicCode = new PublicCode("CustomCode12");
        LockerPassword customLockerPassword = new LockerPassword(
                "$2a$10$ABCDEFGHIJKLMNOPQRSTUVabcdefghijklmnopqrstuvwxyz0123X"
        );

        Account account = validAccountBuilder()
                .role(Role.ADMIN)
                .status(AccountStatus.DELETED)
                .isVerified(true)
                .publicCode(customPublicCode)
                .lockerPassword(customLockerPassword)
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
                () -> Assertions.assertEquals(customPublicCode, account.publicCode()),
                () -> Assertions.assertEquals(customLockerPassword, account.lockerPassword()),
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
        account.verify();

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

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRotatePublicCode() {
        Account account = validAccountBuilder().build();
        PublicCode newPublicCode = new PublicCode("RotatedC0d12");

        account.rotatePublicCode(newPublicCode);

        Assertions.assertEquals(newPublicCode, account.publicCode());
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRejectRotatingPublicCodeForDeletedAccount() {
        Account account = validAccountBuilder()
                .status(AccountStatus.DELETED)
                .build();
        PublicCode newPublicCode = new PublicCode("RotatedC0d12");

        Assertions.assertThrows(AccountConflictException.class, () -> account.rotatePublicCode(newPublicCode));
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldChangeLockerPassword() {
        Account account = validAccountBuilder().build();
        LockerPassword newLockerPassword = new LockerPassword(
                "$2a$10$ABCDEFGHIJKLMNOPQRSTUVabcdefghijklmnopqrstuvwxyz0123X"
        );

        account.changeLockerPassword(newLockerPassword);

        Assertions.assertEquals(newLockerPassword, account.lockerPassword());
    }

    @Test
    @Timeout(1)
    @Tag("account")
    void shouldRejectChangingLockerPasswordForDeletedAccount() {
        Account account = validAccountBuilder()
                .status(AccountStatus.DELETED)
                .build();
        LockerPassword newLockerPassword = new LockerPassword(
                "$2a$10$ABCDEFGHIJKLMNOPQRSTUVabcdefghijklmnopqrstuvwxyz0123X"
        );

        Assertions.assertThrows(AccountConflictException.class, () -> account.changeLockerPassword(newLockerPassword));
    }

    private Account.AccountBuilder validAccountBuilder() {
        return new Account.AccountBuilder()
                .id(ID)
                .email(EMAIL)
                .encodedPassword(PASSWORD)
                .publicCode(PUBLIC_CODE)
                .lockerPassword(LOCKER_PASSWORD);
    }
}