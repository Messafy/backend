package com.luispiquinrey.backend.account.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class PublicCodeTest {

    private static final String VALID_CODE = "V1StGXR8_Z5j";

    @Test
    @Timeout(1)
    @Tag("publicCode")
    void shouldAcceptValidNanoIdPublicCode() {
        Assertions.assertDoesNotThrow(() -> new PublicCode(VALID_CODE));
    }

    @Test
    @Timeout(1)
    @Tag("publicCode")
    void shouldRejectNullPublicCode() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PublicCode(null));
    }

    @Test
    @Timeout(1)
    @Tag("publicCode")
    void shouldRejectBlankPublicCode() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PublicCode(" "));
    }

    @Test
    @Timeout(1)
    @Tag("publicCode")
    void shouldRejectPublicCodeWithInvalidLength() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PublicCode("V1StGXR8_Z5"));
    }

    @Test
    @Timeout(1)
    @Tag("publicCode")
    void shouldRejectPublicCodeWithInvalidCharacters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new PublicCode("V1StGXR8!Z5j"));
    }
}
