package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class NoteIdTest {

    private final String id = "507f1f77bcf86cd799439011";

    @Test
    @Timeout(1)
    @Tag("noteId")
    void shouldThrowIfNoteIdIsBlank() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new NoteId(""));
    }

    @Test
    @Timeout(1)
    @Tag("noteId")
    void shouldThrowIfNoteIdIsNull() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new NoteId(null));
    }

    @Test
    @Timeout(1)
    @Tag("noteId")
    void shouldNotThrowIfNoteIdIsValid() {
        Assertions.assertDoesNotThrow(() -> new NoteId(id));
    }

    @Test
    @Timeout(1)
    @Tag("noteId")
    void shouldThrowWhenNoteIdIsInvalid() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new NoteId("invalid"));
    }
}
