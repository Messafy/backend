package com.luispiquinrey.backend.account.application.service;

import com.luispiquinrey.backend.account.application.port.VerificationTokenCodec;
import com.luispiquinrey.backend.account.domain.exception.AccountNotFoundException;
import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerifyAccountServiceTest {

    @Mock
    private VerificationTokenCodec verificationTokenCodec;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private VerifyAccountService service;

    @Test
    @Timeout(1)
    @Tag("verifyAccountService")
    void shouldIssueTokenThroughCodec() {
        when(verificationTokenCodec.issue("507f1f77bcf86cd799439011")).thenReturn("raw-token");

        String token = service.issue("507f1f77bcf86cd799439011");

        assertEquals("raw-token", token);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountService")
    void shouldVerifyAndSaveAccountForValidToken() {
        Account account = org.mockito.Mockito.mock(Account.class);
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("507f1f77bcf86cd799439011");
        when(accountRepository.findById(new UserId("507f1f77bcf86cd799439011"))).thenReturn(Optional.of(account));

        service.verifyByToken("raw-token");

        verify(account).verify();
        verify(accountRepository).save(account);
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountService")
    void shouldThrowWhenTokenAccountIsMissing() {
        when(verificationTokenCodec.extractAccountId("raw-token")).thenReturn("507f1f77bcf86cd799439099");
        when(accountRepository.findById(new UserId("507f1f77bcf86cd799439099"))).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> service.verifyByToken("raw-token"));

        verify(accountRepository, never()).save(org.mockito.Mockito.any());
    }

    @Test
    @Timeout(1)
    @Tag("verifyAccountService")
    void shouldPropagateInvalidTokenFailure() {
        when(verificationTokenCodec.extractAccountId("broken"))
                .thenThrow(new com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException("invalid"));

        assertThrows(com.luispiquinrey.backend.account.domain.exception.AccountVerificationTokenException.class,
                () -> service.verifyByToken("broken"));
    }
}
