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

public class SmtpMailSender implements MailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailSender.class);

    private final JavaMailSender javaMailSender;
    private final String from;
    private final String verificationUrl;
    private final String template;

    public SmtpMailSender(JavaMailSender javaMailSender, String from, String publicBaseUrl, String template) {
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
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            message.setFrom(new InternetAddress(from));
            message.setRecipients(MimeMessage.RecipientType.TO, to);
            message.setSubject("Welcome to Messafy", "utf-8");
            message.setContent(body, "text/html; charset=utf-8");
            javaMailSender.send(message);
            log.debug("Sent email to {}", to);
        } catch (MessagingException | MailException e) {
            throw new MailDeliveryException(e);
        }
    }
}
