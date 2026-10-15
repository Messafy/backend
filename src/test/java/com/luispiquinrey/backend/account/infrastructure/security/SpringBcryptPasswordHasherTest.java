package com.luispiquinrey.backend.account.infrastructure.security;

import com.luispiquinrey.backend.account.domain.model.EncodedPassword;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpringBcryptPasswordHasherTest {
    @Test
    @Timeout(1)
    @Tag("passwordHasher")
    void supportsDelegatingBcryptEncoderUsedByTestProfile() {
        var hasher = new SpringBcryptPasswordHasher(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        assertTrue(hasher.matches("secret", hasher.hash("secret")));
    }

    @Test
    @Timeout(1)
    @Tag("passwordHasher")
    void rejectsAnEncoderReturningPlaintext() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("secret")).thenReturn("secret");
        assertThrows(IllegalStateException.class, () -> new SpringBcryptPasswordHasher(encoder).hash("secret"));
    }

    @Test
    @Timeout(1)
    @Tag("passwordHasher")
    void verifiesRealHashesAndRejectsInvalidStoredFormats() {
        var hasher = new SpringBcryptPasswordHasher(new BCryptPasswordEncoder(4));
        EncodedPassword hash = hasher.hash("secret");
        assertTrue(hasher.matches("secret", hash));
        assertFalse(hasher.matches("wrong", hash));
        assertFalse(hasher.matches("secret", new EncodedPassword("secret")));
        assertFalse(hasher.matches("secret", new EncodedPassword("$2z$10$invalid")));
        assertFalse(hasher.matches("secret", new EncodedPassword("$2a$10$short")));
    }
}
