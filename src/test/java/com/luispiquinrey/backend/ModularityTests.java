package com.luispiquinrey.backend;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    @Timeout(5)
    @Tag("modularityVerification")
    void verifiesModuleBoundaries() {
        ApplicationModules.of(BackendApplication.class).verify();
    }
}
