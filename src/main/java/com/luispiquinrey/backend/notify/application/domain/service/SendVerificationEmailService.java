package com.luispiquinrey.backend.notify.application.domain.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SendVerificationEmailService implements SendVerificationEmailUseCase {

    private static final Logger log = LoggerFactory.getLogger(SendVerificationEmailService.class);

    @Override
    public void handle(AccountRegistered event) {
        log.info("Verification email pending for account {}", event.accountId());
        // TODO: TokenCodec.issue(event.accountId()) -> link -> MailSender.send(...)
    }
}
