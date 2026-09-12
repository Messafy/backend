package com.luispiquinrey.backend.account.slices.register;

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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoRegisterAccountRepositoryTest {

    @Mock
    private MongoDataAccountRepository baseRepository;

    @Mock
    private AccountMapper mapper;

    @InjectMocks
    private MongoRegisterAccountRepository repository;

    @Test
    @Timeout(1)
    @Tag("mongoRegisterAccountRepository")
    void shouldMapAndPersistAccount() {
        String id = "507f1f77bcf86cd799439011";
        Account account = new Account.AccountBuilder()
                .id(new UserId(id))
                .email(new Email("user@example.com"))
                .encodedPassword(new EncodedPassword("encoded-password"))
                .build();
        AccountDocument document = new AccountDocument(
                new ObjectId(id),
                "user@example.com",
                "encoded-password",
                "USER",
                "ACTIVE",
                false,
                account.createdAt().value().toString(),
                null
        );
        when(mapper.toDocument(account)).thenReturn(document);

        repository.save(account);

        verify(mapper).toDocument(account);
        verify(baseRepository).save(document);
    }
}
