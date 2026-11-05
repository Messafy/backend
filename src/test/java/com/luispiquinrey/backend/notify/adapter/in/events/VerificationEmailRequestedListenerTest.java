package com.luispiquinrey.backend.notify.adapter.in.events;

import com.luispiquinrey.backend.account.api.VerificationEmailRequested;
import com.luispiquinrey.backend.notify.application.dto.SendVerificationEmailRequest;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class VerificationEmailRequestedListenerTest {

    @Test
    @Timeout(1)
    @Tag("verificationEmailRequestedListener")
    void delegatesResendRequestsToTheVerificationEmailUseCase() {
        SendVerificationEmailUseCase useCase = mock(SendVerificationEmailUseCase.class);

        new VerificationEmailRequestedListener(useCase)
                .onVerificationEmailRequested(new VerificationEmailRequested("account-1", "user@example.com"));

        verify(useCase).send(new SendVerificationEmailRequest("account-1", "user@example.com"));
    }
}
