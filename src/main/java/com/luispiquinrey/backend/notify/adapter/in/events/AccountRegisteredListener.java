package com.luispiquinrey.backend.notify.adapter.in.events;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AccountRegisteredListener {

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    public AccountRegisteredListener(SendVerificationEmailUseCase sendVerificationEmailUseCase) {
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
    }

    @Async
    @EventListener
    public void onAccountRegistered(AccountRegistered event) {
        sendVerificationEmailUseCase.handle(event);
    }
}
