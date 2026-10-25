package com.luispiquinrey.backend.notify.adapter.out.mail;

import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class MailSenderConfiguration {

    @Bean
    @Profile("!test")
    @ConditionalOnProperty("spring.mail.host")
    MailSender smtpMailSender(JavaMailSender javaMailSender,
                              @Value("${spring.mail.username}") String from,
                              @Value("${app.public-base-url}") String publicBaseUrl) throws IOException {
        String template = new ClassPathResource("mail/verification.html")
                .getContentAsString(StandardCharsets.UTF_8);
        return new SmtpMailSender(javaMailSender, from, publicBaseUrl, template);
    }
}
