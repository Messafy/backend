package com.luispiquinrey.backend.notify.adapter.in.events;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AccountRegisteredListenerTest {

    @Test
    @Timeout(1)
    @Tag("accountRegisteredListener")
    void shouldDelegateRegisteredAccountsToTheVerificationEmailUseCase() {
        SendVerificationEmailUseCase useCase = mock(SendVerificationEmailUseCase.class);
        AccountRegistered event = new AccountRegistered("507f1f77bcf86cd799439011", "user@example.com");
        AccountRegisteredListener listener = new AccountRegisteredListener(useCase);

        listener.onAccountRegistered(event);

        verify(useCase).handle(event);
    }
}
