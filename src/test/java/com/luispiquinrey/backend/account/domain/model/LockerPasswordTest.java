package com.luispiquinrey.backend.account.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class LockerPasswordTest {

    private static final String VALID_BCRYPT = "$2a$10$7EqJtq98hPqEX7fNZaFWoO5z7c2uR3HF4YDs5H3WwQ1S/9vGQ4r6e";

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldAcceptBcryptEncodedLockerPassword() {
        Assertions.assertDoesNotThrow(() -> new LockerPassword(VALID_BCRYPT));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldRejectNullEncodedLockerPassword() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LockerPassword(null));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldRejectBlankEncodedLockerPassword() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LockerPassword(" "));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldAcceptAnotherHashAlgorithm() {
        Assertions.assertDoesNotThrow(() -> new LockerPassword("$argon2id$v=19$m=65536,t=3,p=1$salt$hash"));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldAcceptRawLockerPasswordAtMinimumLength() {
        Assertions.assertDoesNotThrow(() -> LockerPassword.validateRaw("Locker12"));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldRejectRawLockerPasswordBelowMinimumLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> LockerPassword.validateRaw("Locker1"));
    }

    @Test
    @Timeout(1)
    @Tag("lockerPassword")
    void shouldRejectRawLockerPasswordWithWhitespace() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> LockerPassword.validateRaw("Locker 1"));
    }
}
