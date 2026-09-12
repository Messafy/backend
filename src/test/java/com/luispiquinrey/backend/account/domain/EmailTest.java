package com.luispiquinrey.backend.account.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class EmailTest {

    @Test
    @Timeout(1)
    @Tag("email")
    void shouldThrowIfEmailIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Email(""));
    }

    @Test
    @Timeout(1)
    @Tag("email")
    void shouldThrowIfEmailIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Email(null));
    }

    @Test
    @Timeout(1)
    @Tag("email")
    void shouldThrowIfEmailIsInvalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Email("invalid"));
    }

    @Test
    @Timeout(1)
    @Tag("email")
    void shouldNotThrowIfEmailIsValid() {
        Assertions.assertDoesNotThrow(() -> new Email("test@gmail.com"));
    }
}
