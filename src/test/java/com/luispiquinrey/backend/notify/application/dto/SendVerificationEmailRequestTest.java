package com.luispiquinrey.backend.notify.application.dto;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SendVerificationEmailRequestTest {

    @Test
    @Timeout(1)
    @Tag("sendVerificationEmailRequest")
    void requiresAccountIdAndEmail() {
        assertThrows(IllegalArgumentException.class,
                () -> new SendVerificationEmailRequest(" ", "user@example.com"));
        assertThrows(IllegalArgumentException.class,
                () -> new SendVerificationEmailRequest("account-id", null));
    }
}
