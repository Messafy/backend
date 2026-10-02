package com.luispiquinrey.backend.account.infrastructure.persistence;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.time.Date;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountRepositoryAdapterTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";
    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";
    private static final String ENCODED_LOCKER = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";
    private static final String PUBLIC_CODE = "V1StGXR8_Z5j";

    @Mock
    private MongoDataAccountRepository mongoRepository;

    @Mock
    private AccountMapper mapper;

    @InjectMocks
    private AccountRepositoryAdapter repository;

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldFindAndMapAccountById() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        AccountDocument document = accountDocument(objectId);
        Account account = account();
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(account);

        Optional<Account> result = repository.findById(new UserId(ACCOUNT_ID));

        assertTrue(result.isPresent());
        assertSame(account, result.get());
        verify(mapper).toObjectId(ACCOUNT_ID);
        verify(mongoRepository).findById(objectId);
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldReturnEmptyWhenAccountByIdDoesNotExist() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.empty());

        Optional<Account> result = repository.findById(new UserId(ACCOUNT_ID));

        assertTrue(result.isEmpty());
        verify(mapper).toObjectId(ACCOUNT_ID);
        verify(mongoRepository).findById(objectId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldFindAndMapAccountByEmail() {
        AccountDocument document = accountDocument(new ObjectId(ACCOUNT_ID));
        Account account = account();
        when(mongoRepository.findByEmail(EMAIL)).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(account);

        Optional<Account> result = repository.findByEmail(EMAIL);

        assertTrue(result.isPresent());
        assertSame(account, result.get());
        verify(mongoRepository).findByEmail(EMAIL);
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldReturnEmptyWhenAccountByEmailDoesNotExist() {
        when(mongoRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<Account> result = repository.findByEmail(EMAIL);

        assertTrue(result.isEmpty());
        verify(mongoRepository).findByEmail(EMAIL);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldInsertNewDocumentWhenAccountDoesNotExist() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        Account account = account();
        AccountDocument document = accountDocument(objectId);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.empty());
        when(mapper.toDocument(account)).thenReturn(document);

        repository.save(account);

        verify(mapper).toDocument(account);
        verify(mongoRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldNotMapNewDocumentWhenAccountAlreadyExists() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        Account account = account(new Date(Instant.parse("2026-09-12T08:30:00Z")));
        AccountDocument document = accountDocument(objectId);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(account);

        verify(mapper, never()).toDocument(any());
        verify(mongoRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldUpdateLastLoginOnExistingDocument() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        Account account = account(new Date(Instant.parse("2026-09-12T08:30:00Z")));
        AccountDocument document = accountDocument(objectId);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(account);

        assertEquals("2026-09-12T08:30:00Z", document.getLastLoginAt());
        verify(mongoRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldKeepNullLastLoginOnExistingDocument() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        Account account = account(null);
        AccountDocument document = accountDocument(objectId);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(account);

        assertNull(document.getLastLoginAt());
        verify(mongoRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("accountRepositoryAdapter")
    void shouldUpdateAllFieldsOnExistingDocument() {
        ObjectId objectId = new ObjectId(ACCOUNT_ID);
        Account account = account(new Date(Instant.parse("2026-09-12T08:30:00Z")));
        AccountDocument document = accountDocument(objectId);
        when(mapper.toObjectId(ACCOUNT_ID)).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(account);

        assertEquals(EMAIL, document.getEmail());
        assertEquals(ENCODED_PASSWORD, document.getEncodedPassword());
        assertEquals("USER", document.getRole());
        assertEquals("ACTIVE", document.getAccountStatus());
        assertFalse(document.isVerified());
        assertEquals(PUBLIC_CODE, document.getPublicCode());
        assertEquals(ENCODED_LOCKER, document.getEncodedLockerPassword());
        verify(mongoRepository).save(document);
    }

    private Account account(Date lastLoginAt) {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword(ENCODED_PASSWORD))
                .publicCode(new PublicCode(PUBLIC_CODE))
                .lockerPassword(new LockerPassword(ENCODED_LOCKER))
                .lastLoginAt(lastLoginAt)
                .build();
    }

    private Account account() {
        return account(null);
    }

    private AccountDocument accountDocument(ObjectId objectId) {
        return new AccountDocument(
                objectId,
                EMAIL,
                ENCODED_PASSWORD,
                "USER",
                "ACTIVE",
                true,
                PUBLIC_CODE,
                ENCODED_LOCKER,
                "2026-01-01T10:00:00Z",
                "2026-01-02T10:00:00Z"
        );
    }
}