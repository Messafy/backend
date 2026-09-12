package com.luispiquinrey.backend.share.time;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

class DateTest {

    @Test
    @Timeout(1)
    @Tag("date")
    void shouldThrowIfValueIsNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Date(null)
        );
        assertTrue(ex.getMessage().contains("date cannot be null"));
    }

    @Test
    @Timeout(1)
    @Tag("date")
    void shouldAcceptPastInstant() {
        assertDoesNotThrow(() -> new Date(Instant.now().minus(1, ChronoUnit.DAYS)));
    }

    @Test
    @Timeout(1)
    @Tag("date")
    void shouldAcceptFutureInstant() {
        assertDoesNotThrow(() -> new Date(Instant.now().plus(1, ChronoUnit.DAYS)));
    }

    @Test
    @Timeout(1)
    @Tag("date")
    void shouldProduceNowCloseToSystemClock() {
        Instant before = Instant.now();
        Date now = Date.now();
        Instant after = Instant.now();
        Instant produced = now.value();
        assertEquals(false, produced.isBefore(before));
        assertEquals(false, produced.isAfter(after));
    }

    @Test
    @Timeout(1)
    @Tag("date")
    void shouldEqualWhenSameInstant() {
        Instant instant = Instant.parse("2026-01-15T10:00:00Z");
        Date a = new Date(instant);
        Date b = new Date(instant);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
