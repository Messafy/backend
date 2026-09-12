package com.luispiquinrey.backend.account.slices.login;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.infrastructure.AccountDocument;
import com.luispiquinrey.backend.account.infrastructure.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.MongoDataAccountRepository;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoLoginAccountRepositoryTest {

    @Mock
    private AccountMapper mapper;

    @Mock
    private MongoDataAccountRepository mongoRepository;

    @InjectMocks
    private MongoLoginAccountRepository repository;

    @Test
    @Timeout(1)
    @Tag("mongoLoginAccountRepository")
    void shouldFindAndMapAccountByEmail() {
        AccountDocument document = accountDocument();
        Account account = account(new Date(Instant.parse("2026-09-12T08:30:00Z")));
        when(mongoRepository.findByEmail("user@example.com")).thenReturn(Optional.of(document));
        when(mapper.toDomain(document)).thenReturn(account);

        Optional<Account> result = repository.byEmail("user@example.com");

        assertTrue(result.isPresent());
        assertSame(account, result.get());
        verify(mongoRepository).findByEmail("user@example.com");
        verify(mapper).toDomain(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoLoginAccountRepository")
    void shouldReturnEmptyWhenAccountDoesNotExist() {
        when(mongoRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        Optional<Account> result = repository.byEmail("missing@example.com");

        assertTrue(result.isEmpty());
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @Timeout(1)
    @Tag("mongoLoginAccountRepository")
    void shouldUpdateLastLoginAndPersistDocument() {
        Date lastLoginAt = new Date(Instant.parse("2026-09-12T08:30:00Z"));
        Account account = account(lastLoginAt);
        AccountDocument document = accountDocument();
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.of(document));

        repository.save(account);

        assertEquals("2026-09-12T08:30:00Z", document.getLastLoginAt());
        verify(mongoRepository).save(document);
    }

    @Test
    @Timeout(1)
    @Tag("mongoLoginAccountRepository")
    void shouldThrowWhenSavingMissingAccount() {
        Account account = account(new Date(Instant.parse("2026-09-12T08:30:00Z")));
        ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
        when(mapper.toObjectId("507f1f77bcf86cd799439011")).thenReturn(objectId);
        when(mongoRepository.findById(objectId)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> repository.save(account));

        verify(mongoRepository, never()).save(any());
    }

    private Account account(Date lastLoginAt) {
        return new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .lastLoginAt(lastLoginAt)
                .build();
    }

    private AccountDocument accountDocument() {
        return new AccountDocument(
                new ObjectId("507f1f77bcf86cd799439011"),
                "user@example.com",
                "encoded-password",
                "USER",
                "ACTIVE",
                true,
                "2026-01-01T10:00:00Z",
                "2026-01-02T10:00:00Z"
        );
    }
}
