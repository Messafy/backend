package com.luispiquinrey.backend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.aop.support.AopUtils;
import com.luispiquinrey.backend.account.api.AccountLookup;
import com.luispiquinrey.backend.notes.application.service.GetNoteService;
import com.luispiquinrey.backend.notify.application.port.in.SendVerificationEmailUseCase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;

@SpringBootTest
class BackendApplicationTests {

    @MockitoBean
    private MailSender mailSender;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
    }

    @Test
    @Timeout(1)
    @Tag("profileConfiguration")
    void testContextHasNoSmtpOrOtlpExporterAndHasUsableCredentials() {
        org.junit.jupiter.api.Assertions.assertTrue(
                context.getBeansOfType(org.springframework.mail.javamail.JavaMailSender.class).isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(
                context.getBeansOfType(io.micrometer.registry.otlp.OtlpMeterRegistry.class).isEmpty());
        var env = context.getEnvironment();
        var user = context.getBean(org.springframework.security.core.userdetails.UserDetailsService.class)
                .loadUserByUsername(env.getProperty("security.user.username"));
        var encoder = context.getBean(org.springframework.security.crypto.password.PasswordEncoder.class);
        org.junit.jupiter.api.Assertions.assertTrue(encoder.matches(
                env.getProperty("USER_TEST_PASSWORD", "test"), user.getPassword()));
    }

    @Test
    @Timeout(1)
    @Tag("executionLogging")
    void applicationServicesAreInstrumentedAcrossContexts() {
        org.junit.jupiter.api.Assertions.assertTrue(AopUtils.isAopProxy(context.getBean(AccountLookup.class)));
        org.junit.jupiter.api.Assertions.assertTrue(AopUtils.isAopProxy(context.getBean(GetNoteService.class)));
        org.junit.jupiter.api.Assertions.assertTrue(AopUtils.isAopProxy(context.getBean(SendVerificationEmailUseCase.class)));
    }

}
