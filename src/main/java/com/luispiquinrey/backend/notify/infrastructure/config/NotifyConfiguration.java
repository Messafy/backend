package com.luispiquinrey.backend.notify.infrastructure.config;

import com.luispiquinrey.backend.notify.application.service.SendVerificationEmailService;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration(proxyBeanMethods = false)
@EnableAsync
@Import(SendVerificationEmailService.class)
public class NotifyConfiguration {
}
