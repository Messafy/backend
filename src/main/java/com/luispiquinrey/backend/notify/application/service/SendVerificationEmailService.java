package com.luispiquinrey.backend.notify.application.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

@LogExecution
public class SendVerificationEmailService implements SendVerificationEmailUseCase {
    private final MailSender mailSender;
    private final VerificationTokenIssuer verificationTokenIssuer;
    private final Logger log;

    public SendVerificationEmailService(MailSender mailSender, VerificationTokenIssuer verificationTokenIssuer, Logger log) {
        this.mailSender = mailSender;
        this.verificationTokenIssuer = verificationTokenIssuer;
        this.log = log;
    }

    @Override
    public void handle(AccountRegistered event) {
        log.debug("Sending verification email for account {}", event.accountId());
        mailSender.sendVerification(event.email(), verificationTokenIssuer.issue(event.accountId()));
        log.debug("Sent verification email for account {}", event.accountId());
    }
}
