package com.luispiquinrey.backend.notify.application.service;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import com.luispiquinrey.backend.share.logging.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendVerificationEmailServiceTest {

    private static final String ACCOUNT_ID = "507f1f77bcf86cd799439011";
    private static final String EMAIL = "user@example.com";
    private static final String TOKEN = "verification-token";

    @Test
    @Timeout(1)
    @Tag("sendVerificationEmailService")
    void shouldSendAVerificationLinkToRegisteredAccounts() {
        MailSender mailSender = mock(MailSender.class);
        VerificationTokenIssuer tokenIssuer = mock(VerificationTokenIssuer.class);
        when(tokenIssuer.issue(ACCOUNT_ID)).thenReturn(TOKEN);
        SendVerificationEmailService service = new SendVerificationEmailService(mailSender, tokenIssuer, mock(Logger.class));

        service.handle(new AccountRegistered(ACCOUNT_ID, EMAIL));

        verify(tokenIssuer).issue(ACCOUNT_ID);
        verify(mailSender).sendVerification(EMAIL, TOKEN);
    }
}
