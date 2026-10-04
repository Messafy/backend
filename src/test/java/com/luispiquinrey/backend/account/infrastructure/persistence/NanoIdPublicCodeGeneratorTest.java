package com.luispiquinrey.backend.account.infrastructure.persistence;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class NanoIdPublicCodeGeneratorTest {

    @Test
    @Timeout(1)
    @Tag("nanoIdPublicCodeGenerator")
    void shouldGenerateTwelveCharacterUrlSafePublicCode() {
        String code = new NanoIdPublicCodeGenerator().nextCode();

        Assertions.assertEquals(12, code.length());
        Assertions.assertTrue(code.matches("^[A-Za-z0-9_-]{12}$"));
    }
}
