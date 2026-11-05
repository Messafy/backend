package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.Account;
import com.luispiquinrey.backend.account.domain.model.Email;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import com.luispiquinrey.backend.account.domain.model.LockerPassword;
import com.luispiquinrey.backend.account.domain.model.PublicCode;
import com.luispiquinrey.backend.account.domain.repository.AccountRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountUserDetailsServiceTest {

    private static final String EMAIL = "user@example.com";

    @Test
    @Timeout(1)
    @Tag("accountUserDetailsService")
    void cachesFoundAccountsForTheConfiguredTtl() {
        AccountRepository repository = mock(AccountRepository.class);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account()));
        var service = new AccountUserDetailsService(repository, Duration.ofMinutes(1));

        service.loadUserByUsername(EMAIL);
        var details = service.loadUserByUsername(EMAIL);

        assertEquals(EMAIL, details.getUsername());
        verify(repository, times(1)).findByEmail(EMAIL);
    }

    @Test
    @Timeout(1)
    @Tag("accountUserDetailsService")
    void readsTheRepositoryEveryTimeWhenTheCacheIsDisabled() {
        AccountRepository repository = mock(AccountRepository.class);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.of(account()));
        var service = new AccountUserDetailsService(repository, Duration.ZERO);

        service.loadUserByUsername(EMAIL);
        service.loadUserByUsername(EMAIL);

        verify(repository, times(2)).findByEmail(EMAIL);
    }

    @Test
    @Timeout(1)
    @Tag("accountUserDetailsService")
    void neverCachesMissingAccounts() {
        AccountRepository repository = mock(AccountRepository.class);
        when(repository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        var service = new AccountUserDetailsService(repository, Duration.ofMinutes(1));

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername(EMAIL));
        verify(repository, times(2)).findByEmail(EMAIL);
    }

    private Account account() {
        return new Account.AccountBuilder()
                .id(new UserId("507f1f77bcf86cd799439011"))
                .email(new Email(EMAIL))
                .encodedPassword(new EncodedPassword("encoded-for-test"))
                .publicCode(new PublicCode("V1StGXR8_Z5j"))
                .lockerPassword(new LockerPassword("encoded-locker-for-test"))
                .isVerified(true)
                .build();
    }
}
