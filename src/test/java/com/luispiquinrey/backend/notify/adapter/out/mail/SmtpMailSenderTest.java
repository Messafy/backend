package com.luispiquinrey.backend.notify.adapter.out.mail;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailSendException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

class SmtpMailSenderTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP.dynamicPort());

    @Test
    @Timeout(3)
    @Tag("smtpMailSender")
    void shouldSendHtmlEmailThroughSmtp() throws Exception {
        String template = new org.springframework.core.io.ClassPathResource("mail/verification.html")
                .getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        SmtpMailSender sender = new SmtpMailSender(javaMailSender(), "noreply@messafy.com", "https://api.example.com", template);

        sender.sendVerification("user@example.com", "token+with&symbols");

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals("noreply@messafy.com", message.getFrom()[0].toString());
        assertEquals("user@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Welcome to Messafy", message.getSubject());
        assertTrue(message.getContentType().contains("text/html"));
        assertTrue(message.getContent().toString().contains("https://api.example.com/v1/auth/verify?token=token%2Bwith%26symbols"));
    }

    private JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(greenMail.getSmtp().getPort());
        return sender;
    }

    @Test
    @Timeout(1)
    @Tag("smtpMailSender")
    void wrapsTechnicalDeliveryFailure() {
        JavaMailSender transport = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage(jakarta.mail.Session.getInstance(new java.util.Properties()));
        when(transport.createMimeMessage()).thenReturn(message);
        doThrow(new MailSendException("SMTP unavailable")).when(transport).send(message);
        var sender = new SmtpMailSender(transport, "noreply@example.com", "https://api.example.com", "__LINK__");
        assertThrows(MailDeliveryException.class, () -> sender.sendVerification("user@example.com", "token"));
    }
}
