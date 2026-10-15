package com.luispiquinrey.backend.account.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class EncodedPasswordTest {

    private static final String VALID_BCRYPT = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

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

    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldAcceptBcryptEncodedPassword() {
        Assertions.assertDoesNotThrow(() -> new EncodedPassword(VALID_BCRYPT));
    }

    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldAcceptAnotherHashAlgorithm() {
        Assertions.assertDoesNotThrow(() -> new EncodedPassword("$argon2id$v=19$m=65536,t=3,p=1$salt$hash"));
    }

    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldRejectWhitespaceOnly() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new EncodedPassword(" \t")
        );
    }

    @Test
    @Timeout(1)
    @Tag("encodedPassword")
    void shouldPreserveEncodedValue() {
        Assertions.assertEquals(VALID_BCRYPT, new EncodedPassword(VALID_BCRYPT).value());
    }
}
