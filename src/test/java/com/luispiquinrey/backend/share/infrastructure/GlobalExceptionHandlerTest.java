package com.luispiquinrey.backend.share.infrastructure;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    @Test
    @Timeout(1)
    @Tag("globalExceptionHandler")
    void technicalStateFailureIsNotExposedAsBusinessConflict() {
        var response = new GlobalExceptionHandler().handleIllegalState(new IllegalStateException("internal details"));
        assertEquals(500, response.getStatus());
        assertEquals("An internal error occurred", response.getDetail());
    }
}
