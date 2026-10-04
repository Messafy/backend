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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmtpMailSenderTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP.dynamicPort());

    @Test
    @Timeout(3)
    @Tag("smtpMailSender")
    void shouldSendHtmlEmailThroughSmtp() throws Exception {
        SmtpMailSender sender = new SmtpMailSender(javaMailSender(), "noreply@messafy.com");

        sender.send("user@example.com", "Welcome to Messafy", "<h1>Welcome</h1>");

        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        MimeMessage message = messages[0];
        assertEquals("noreply@messafy.com", message.getFrom()[0].toString());
        assertEquals("user@example.com", message.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Welcome to Messafy", message.getSubject());
        assertTrue(message.getContentType().contains("text/html"));
        assertEquals("<h1>Welcome</h1>", message.getContent());
    }

    private JavaMailSenderImpl javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("127.0.0.1");
        sender.setPort(greenMail.getSmtp().getPort());
        return sender;
    }
}
