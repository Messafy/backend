package com.luispiquinrey.backend.notify.adapter.out.mail;

import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailSenderConfiguration {

    @Bean
    @ConditionalOnProperty("spring.mail.host")
    MailSender smtpMailSender(JavaMailSender javaMailSender,
                              @Value("${spring.mail.username}") String from) {
        return new SmtpMailSender(javaMailSender, from);
    }
}
