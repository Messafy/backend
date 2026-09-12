package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class ContentTest {
    @Test
    @Timeout(1)
    @Tag("content")
    void shouldThrowIfContentIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Content(""));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldThrowIfContentIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Content(null));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldNotThrowIfContentIsNotEmpty() {
        Assertions.assertDoesNotThrow(() -> new Content("content"));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldThrowIfContentHasMoreThan1000Characters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Content("a".repeat(1001)));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldNotThrowIfContentHasLessThan1000Characters() {
        Assertions.assertDoesNotThrow(() -> new Content("a".repeat(999)));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldThrowIfContentContainsUrls() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Content("https://luispiquinrey.com"));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldThrowIfContentContainsControlCharacters() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new Content("content\u0000"));
    }

    @Test
    @Timeout(1)
    @Tag("content")
    void shouldNotThrowIfContentContainsAllowedWhitespaceControlCharacters() {
        Assertions.assertDoesNotThrow(() -> new Content("line 1\nline 2\tindented\r"));
    }
}
