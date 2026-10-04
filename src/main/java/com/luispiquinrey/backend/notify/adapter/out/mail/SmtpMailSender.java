package com.luispiquinrey.backend.notify.adapter.out.mail;

import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSender.class);

    private final JavaMailSender javaMailSender;
    private final String from;

    public SmtpMailSender(JavaMailSender javaMailSender, String from) {
        this.javaMailSender = javaMailSender;
        this.from = from;
    }

    @Override
    public void send(String to, String subject, String body) {
        log.debug("Sending email to {}", to);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setFrom(new InternetAddress(from));
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject(subject, "utf-8");
            message.setContent(body, "text/html; charset=utf-8");
            javaMailSender.send(message);
            log.debug("Sent email to {}", to);
        } catch (MessagingException e) {
            log.warn("Failed to build email for {}: {}", to, e.getMessage());
            throw new IllegalStateException("Failed to build email for " + to, e);
        } catch (MailException e) {
            log.warn("Failed to send email to {}: {}", to, e.getMessage());
            throw new IllegalStateException("Failed to send email to " + to, e);
        }
    }
}
