package com.luispiquinrey.backend.notify.adapter.in.events;

import com.luispiquinrey.backend.account.api.VerificationEmailRequested;
import com.luispiquinrey.backend.notify.application.dto.SendVerificationEmailRequest;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class VerificationEmailRequestedListener {

    private static final Logger log = LoggerFactory.getLogger(VerificationEmailRequestedListener.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;

    public VerificationEmailRequestedListener(SendVerificationEmailUseCase sendVerificationEmailUseCase) {
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
    }

    @Async
    @EventListener
    public void onVerificationEmailRequested(VerificationEmailRequested event) {
        log.debug("Handling verification email request for account {}", event.accountId());
        sendVerificationEmailUseCase.send(
                new SendVerificationEmailRequest(event.accountId(), event.email()));
    }
}
