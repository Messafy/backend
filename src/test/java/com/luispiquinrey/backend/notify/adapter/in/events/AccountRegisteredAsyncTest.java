package com.luispiquinrey.backend.notify.adapter.in.events;

import com.luispiquinrey.backend.account.api.AccountRegistered;
import com.luispiquinrey.backend.account.api.VerificationTokenIssuer;
import com.luispiquinrey.backend.notify.adapter.out.mail.MailSenderConfiguration;
import com.luispiquinrey.backend.notify.infrastructure.config.NotifyConfiguration;
import com.luispiquinrey.backend.notify.adapter.out.mail.MailDeliveryException;
import com.luispiquinrey.backend.notify.application.port.out.MailSender;
import com.luispiquinrey.backend.share.logging.Logger;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class AccountRegisteredAsyncTest {
    @Test
    @Timeout(5)
    @Tag("accountRegisteredAsync")
    void mailFailureDoesNotPropagateToRegistrationThread() throws Exception {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            var config = context.getBean(Config.class);
            Thread publisher = Thread.currentThread();
            assertDoesNotThrow(() -> context.publishEvent(new AccountRegistered("account-1", "user@example.com")));
            assertTrue(config.delivered.await(2, TimeUnit.SECONDS));
            assertNotSame(publisher, config.mailThread.get());
        }
    }

    @TestConfiguration
    @Import({
            MailSenderConfiguration.class,
            NotifyConfiguration.class,
            AccountRegisteredListener.class
    })
    static class Config {
        final CountDownLatch delivered = new CountDownLatch(1);
        final AtomicReference<Thread> mailThread = new AtomicReference<>();

        @Bean
        MailSender mailSender() {
            return (to, token) -> {
                mailThread.set(Thread.currentThread());
                delivered.countDown();
                throw new MailDeliveryException(new RuntimeException("SMTP unavailable"));
            };
        }

        @Bean
        VerificationTokenIssuer tokens() {
            return accountId -> "test-token";
        }

        @Bean
        Logger logger() {
            return mock(Logger.class);
        }
    }
}
