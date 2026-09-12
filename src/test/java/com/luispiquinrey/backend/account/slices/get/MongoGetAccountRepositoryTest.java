package com.luispiquinrey.backend.account.slices.get;

import com.luispiquinrey.backend.account.domain.Account;
import com.luispiquinrey.backend.account.domain.Email;
import com.luispiquinrey.backend.account.domain.EncodedPassword;
import com.luispiquinrey.backend.account.infrastructure.AccountDocument;
import com.luispiquinrey.backend.account.infrastructure.AccountMapper;
import com.luispiquinrey.backend.account.infrastructure.MongoDataAccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoGetAccountRepositoryTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";

    @Mock
    private AccountMapper mapper;

    @Mock
    private MongoDataAccountRepository mongoRepository;

    @InjectMocks
    private MongoGetAccountRepository repository;

    @Test
    @Timeout(1)
    @Tag("mongoFindAccountByIdPresent")
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
    @Tag("mongoFindAccountByIdEmpty")
    void shouldReturnEmptyWhenIdDoesNotExist() {
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
    @Tag("mongoFindAccountByEmailPresent")
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
    @Tag("mongoFindAccountByEmailEmpty")
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        when(mongoRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        Optional<Account> result = repository.findByEmail(EMAIL);

        assertTrue(result.isEmpty());
        verify(mongoRepository).findByEmail(EMAIL);
        verify(mapper, never()).toDomain(any());
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId(ACCOUNT_ID))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .build();
    }

    private AccountDocument accountDocument(ObjectId objectId) {
        return new AccountDocument(
                objectId,
                EMAIL,
                "encoded-password",
                "USER",
                "ACTIVE",
                true,
                "2026-01-01T10:00:00Z",
                "2026-01-02T10:00:00Z"
        );
    }
}
