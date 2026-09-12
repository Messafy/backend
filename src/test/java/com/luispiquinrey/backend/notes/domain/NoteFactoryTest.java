package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class NoteFactoryTest {
    private final NoteFactory noteFactory = new NoteFactory();

    @Test
    @Timeout(1)
    @Tag("notefactory")
    void shouldCreatePrivateNote() {
        PrivateNote note = noteFactory.createPrivateNote(
                "507f1f77bcf86cd799439011",
                "title",
                "content",
                "507f1f77bcf86cd799439012"
        );
        Assertions.assertNotNull(note);
        Assertions.assertEquals("507f1f77bcf86cd799439011", note.id().id());
        Assertions.assertEquals("title", note.title().title());
        Assertions.assertEquals("content", note.content().content());
        Assertions.assertEquals("507f1f77bcf86cd799439012", note.ownerId().id());
    }

    @Test
    @Timeout(1)
    @Tag("notefactory")
    void shouldCreateSharedNote() {
        SharedNote note = noteFactory.createSharedNote(
                "507f1f77bcf86cd799439011",
                "title",
                "content",
                "507f1f77bcf86cd799439012",
                "507f1f77bcf86cd799439013"
        );

        Assertions.assertNotNull(note);
        Assertions.assertEquals("507f1f77bcf86cd799439011", note.id().id());
        Assertions.assertEquals("title", note.title().title());
        Assertions.assertEquals("content", note.content().content());
        Assertions.assertEquals("507f1f77bcf86cd799439012", note.ownerId().id());
        Assertions.assertEquals("507f1f77bcf86cd799439013", note.sharedWith().ownerId().id());
    }
}
