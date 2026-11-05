package com.luispiquinrey.backend.notify.adapter.out.mail;

import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;

public class SmtpMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSender.class);

    private final JavaMailSender javaMailSender;
    private final String from;
    private final String verificationUrl;
    private final String template;
    private final int maxAttempts;
    private final Duration initialBackoff;

    public SmtpMailSender(JavaMailSender javaMailSender, String from, String publicBaseUrl, String template) {
        this(javaMailSender, from, publicBaseUrl, template, 1, Duration.ZERO);
    }

    public SmtpMailSender(JavaMailSender javaMailSender, String from, String publicBaseUrl, String template,
                          int maxAttempts, Duration initialBackoff) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("mail delivery needs at least one attempt");
        }
        if (initialBackoff == null || initialBackoff.isNegative()) {
            throw new IllegalArgumentException("mail retry backoff cannot be negative");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.javaMailSender = javaMailSender;
        this.from = from;
        var base = URI.create(publicBaseUrl);
        if (base.getHost() == null || !("http".equals(base.getScheme()) || "https".equals(base.getScheme()))) {
            throw new IllegalArgumentException("public base URL must be an absolute HTTP(S) URL");
        }
        this.verificationUrl = publicBaseUrl.replaceAll("/+$", "") + "/v1/auth/verify";
        this.template = template;
    }

    @Override
    public void sendVerification(String to, String token) {
        String link = UriComponentsBuilder.fromUriString(verificationUrl)
                .queryParam("token", "{token}").encode().buildAndExpand(token).toUriString();
        String body = template.replace("__LINK__", HtmlUtils.htmlEscape(link));
        log.debug("Sending email to {}", to);
        Duration backoff = initialBackoff;
        for (int attempt = 1; ; attempt++) {
            try {
                send(to, body);
                log.debug("Sent email to {}", to);
                return;
            } catch (MailDeliveryException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                log.warn("Email delivery attempt {} of {} failed; retrying in {} ms",
                        attempt, maxAttempts, backoff.toMillis());
                pause(backoff);
                backoff = backoff.multipliedBy(2);
            }
        }
    }

    private void send(String to, String body) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setFrom(new InternetAddress(from));
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject("Welcome to Messafy", "utf-8");
            message.setContent(body, "text/html; charset=utf-8");
            javaMailSender.send(message);
        } catch (MessagingException | MailException e) {
            throw new MailDeliveryException(e);
        }
    }

    private static void pause(Duration duration) {
        if (duration.isZero()) {
            return;
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MailDeliveryException(e);
        }
    }
}
