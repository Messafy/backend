package com.luispiquinrey.backend.account.infrastructure;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.AccountStatus;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.domain.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

class AccountMapperTest {

    private final AccountMapper mapper = new AccountMapper();

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldConvertCompleteDocumentToDomain() {
        AccountDocument document = new AccountDocument(
                new ObjectId("507f1f77bcf86cd799439011"),
                "owner@example.com",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                "ADMIN",
                "DELETED",
                true,
                "2026-01-01T10:00:00Z",
                "2026-02-03T14:15:16Z"
        );

        Account account = mapper.toDomain(document);

        Assertions.assertAll(
                () -> Assertions.assertEquals("507f1f77bcf86cd799439011", account.id().id()),
                () -> Assertions.assertEquals("owner@example.com", account.email().email()),
                () -> Assertions.assertEquals(
                        "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                        account.encodedPassword().value()
                ),
                () -> Assertions.assertEquals(Role.ADMIN, account.role()),
                () -> Assertions.assertEquals(AccountStatus.DELETED, account.status()),
                () -> Assertions.assertTrue(account.isVerified()),
                () -> Assertions.assertEquals(Instant.parse("2026-01-01T10:00:00Z"), account.createdAt().value()),
                () -> Assertions.assertEquals(Instant.parse("2026-02-03T14:15:16Z"), account.lastLoginAt().value())
        );
    }

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldConvertCompleteDomainToDocument() {
        Account account = new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439012"))
                .email(new Email("admin@example.com"))
                .encodedPassword(new EncodedPassword(
                        "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"
                ))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .createdAt(new Date(Instant.parse("2026-03-04T05:06:07Z")))
                .lastLoginAt(new Date(Instant.parse("2026-04-05T06:07:08Z")))
                .build();

        AccountDocument document = mapper.toDocument(account);

        Assertions.assertAll(
                () -> Assertions.assertEquals("507f1f77bcf86cd799439012", document.getId().toString()),
                () -> Assertions.assertEquals("admin@example.com", document.getEmail()),
                () -> Assertions.assertEquals(
                        "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e",
                        document.getEncodedPassword()
                ),
                () -> Assertions.assertEquals("ADMIN", document.getRole()),
                () -> Assertions.assertEquals("ACTIVE", document.getAccountStatus()),
                () -> Assertions.assertTrue(document.isVerified()),
                () -> Assertions.assertEquals("2026-03-04T05:06:07Z", document.getCreatedAt()),
                () -> Assertions.assertEquals("2026-04-05T06:07:08Z", document.getLastLoginAt())
        );
    }

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldKeepNullOptionalDateWhenConvertingDocumentToDomain() {
        AccountDocument document = new AccountDocument(
                new ObjectId("507f1f77bcf86cd799439013"),
                "user@example.com",
                "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
                "USER",
                "ACTIVE",
                false,
                "2026-05-06T07:08:09Z",
                null
        );

        Account account = mapper.toDomain(document);

        Assertions.assertNull(account.lastLoginAt());
    }

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldKeepNullOptionalDateWhenConvertingDomainToDocument() {
        Account account = new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439014"))
                .email(new Email("member@example.com"))
                .encodedPassword(new EncodedPassword(
                        "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e"
                ))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .createdAt(new Date(Instant.parse("2026-06-07T08:09:10Z")))
                .lastLoginAt(null)
                .build();

        AccountDocument document = mapper.toDocument(account);

        Assertions.assertNull(document.getLastLoginAt());
    }

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldRejectNullArguments() {
        Assertions.assertAll(
                () -> Assertions.assertThrows(NullPointerException.class, () -> mapper.toDomain(null)),
                () -> Assertions.assertThrows(NullPointerException.class, () -> mapper.toDocument(null)),
                () -> Assertions.assertThrows(NullPointerException.class, () -> mapper.toObjectId(null))
        );
    }
}
