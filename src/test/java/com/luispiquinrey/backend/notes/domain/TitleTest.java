package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class TitleTest {

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldThrowIfTitleIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Title(""));
    }

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldThrowIfTitleIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Title(null));
    }

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldNotThrowIfTitleIsValid() {
        Assertions.assertDoesNotThrow(() -> new Title("Private note title"));
    }

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldThrowIfTitleHasMoreThan100Characters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Title("a".repeat(101)));
    }

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldNotThrowIfTitleHas100Characters() {
        Assertions.assertDoesNotThrow(() -> new Title("a".repeat(100)));
    }

    @Test
    @Timeout(1)
    @Tag("title")
    void shouldThrowIfTitleContainsNewLines() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Title("first line\nsecond line"));
    }
}
