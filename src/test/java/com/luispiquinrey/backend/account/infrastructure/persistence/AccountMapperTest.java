package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.AccountStatus;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.model.Role;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;

class AccountMapperTest {

    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String ENCODED_LOCKER = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";

    private final AccountMapper mapper = new AccountMapper();

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldConvertCompleteDocumentToDomain() {
        AccountDocument document = new AccountDocument(
                new ObjectId("507f1f77bcf86cd799439011"),
                "owner@example.com",
                ENCODED_PASSWORD,
                "ADMIN",
                "DELETED",
                true,
                PUBLIC_CODE,
                ENCODED_LOCKER,
                "2026-01-01T10:00:00Z",
                "2026-02-03T14:15:16Z"
        );

        Account account = mapper.toDomain(document);

        Assertions.assertAll(
                () -> Assertions.assertEquals("507f1f77bcf86cd799439011", account.id().id()),
                () -> Assertions.assertEquals("owner@example.com", account.email().email()),
                () -> Assertions.assertEquals(ENCODED_PASSWORD, account.encodedPassword().value()),
                () -> Assertions.assertEquals(Role.ADMIN, account.role()),
                () -> Assertions.assertEquals(AccountStatus.DELETED, account.status()),
                () -> Assertions.assertTrue(account.isVerified()),
                () -> Assertions.assertEquals(PUBLIC_CODE, account.publicCode().value()),
                () -> Assertions.assertEquals(ENCODED_LOCKER, account.lockerPassword().value()),
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
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .role(Role.ADMIN)
                .status(AccountStatus.ACTIVE)
                .isVerified(true)
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .createdAt(new Date(Instant.parse("2026-03-04T05:06:07Z")))
                .lastLoginAt(new Date(Instant.parse("2026-04-05T06:07:08Z")))
                .build();

        AccountDocument doc = mapper.toDocument(account);

        Assertions.assertAll(
                () -> Assertions.assertEquals("507f1f77bcf86cd799439012", doc.getId().toString()),
                () -> Assertions.assertEquals("admin@example.com", doc.getEmail()),
                () -> Assertions.assertEquals(ENCODED_PASSWORD, doc.getEncodedPassword()),
                () -> Assertions.assertEquals("ADMIN", doc.getRole()),
                () -> Assertions.assertEquals("ACTIVE", doc.getAccountStatus()),
                () -> Assertions.assertTrue(doc.isVerified()),
                () -> Assertions.assertEquals(PUBLIC_CODE, doc.getPublicCode()),
                () -> Assertions.assertEquals(ENCODED_LOCKER, doc.getEncodedLockerPassword()),
                () -> Assertions.assertEquals("2026-03-04T05:06:07Z", doc.getCreatedAt()),
                () -> Assertions.assertEquals("2026-04-05T06:07:08Z", doc.getLastLoginAt())
        );
    }

    @Test
    @Tag("accountMapper")
    @Timeout(1)
    void shouldKeepNullOptionalDateWhenConvertingDocumentToDomain() {
        AccountDocument document = new AccountDocument(
                new ObjectId("507f1f77bcf86cd799439013"),
                "user@example.com",
                ENCODED_PASSWORD,
                "USER",
                "ACTIVE",
                false,
                PUBLIC_CODE,
                ENCODED_LOCKER,
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
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .createdAt(new Date(Instant.parse("2026-06-07T08:09:10Z")))
                .lastLoginAt(null)
                .build();

        AccountDocument doc = mapper.toDocument(account);

        Assertions.assertNull(doc.getLastLoginAt());
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