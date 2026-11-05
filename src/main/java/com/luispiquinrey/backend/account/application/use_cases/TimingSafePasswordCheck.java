package com.luispiquinrey.backend.account.application.use_cases;

import com.luispiquinrey.backend.account.application.port.PasswordHasher;
import com.luispiquinrey.backend.account.domain.model.EncodedPassword;

final class TimingSafePasswordCheck {

    private static final String DUMMY_PASSWORD = "timing-safe-dummy-password";

    private final PasswordHasher passwordHasher;
    private volatile EncodedPassword dummyHash;

    TimingSafePasswordCheck(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    boolean matches(String rawPassword, EncodedPassword storedHash) {
        String raw = rawPassword == null ? "" : rawPassword;
        if (storedHash == null) {
            EncodedPassword dummy = dummyHash();
            if (dummy != null) {
                passwordHasher.matches(raw, dummy);
            }
            return false;
        }
        return passwordHasher.matches(raw, storedHash);
    }

    private EncodedPassword dummyHash() {
        EncodedPassword hash = dummyHash;
        if (hash == null) {
            hash = passwordHasher.hash(DUMMY_PASSWORD);
            dummyHash = hash;
        }
        return hash;
    }
}
