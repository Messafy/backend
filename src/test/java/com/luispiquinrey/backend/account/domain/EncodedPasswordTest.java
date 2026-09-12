package com.luispiquinrey.backend.account.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class EncodedPasswordTest {
    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldThrowIfPasswordIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EncodedPassword(""));
    }

    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldThrowIfPasswordIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EncodedPassword(null));
    }
}
