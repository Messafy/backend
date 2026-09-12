package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

public class SharedNoteTest {
    private final NoteFactory noteFactory = new NoteFactory();
    private final String noteId = "507f1f77bcf86cd799439011";
    private final String ownerId = "507f1f77bcf86cd799439012";
    private final String sharedWith = "507f1f77bcf86cd799439013";

    @Test
    @Timeout(1)
    @Tag("sharedNote")
    void shouldCreateSharedNoteWithInitialState() {
        SharedNote note = sharedNote();

        Assertions.assertEquals(noteId, note.id().id());
        Assertions.assertEquals("title", note.title().title());
        Assertions.assertEquals("content", note.content().content());
        Assertions.assertEquals(ownerId, note.ownerId().id());
        Assertions.assertEquals(sharedWith, note.sharedWith().ownerId().id());
        Assertions.assertEquals(NoteStatus.NEW, note.status());
        Assertions.assertNotNull(note.createdAt());
    }

    @Test
    @Timeout(1)
    @Tag("sharedNote")
    void shouldKnowIfSharedNoteIsOwnedByUser() {
        SharedNote note = sharedNote();

        Assertions.assertTrue(note.isOwnedBy(ownerId));
    }

    @Test
    @Timeout(1)
    @Tag("sharedNote")
    void shouldKnowIfSharedNoteIsNotOwnedByUser() {
        SharedNote note = sharedNote();

        Assertions.assertFalse(note.isOwnedBy("507f1f77bcf86cd799439014"));
    }

    @Test
    @Timeout(1)
    @Tag("sharedNote")
    void shouldKnowIfSharedNoteIsSharedWithUser() {
        SharedNote note = sharedNote();

        Assertions.assertTrue(note.isSharedWith(sharedWith));
    }

    @Test
    @Timeout(1)
    @Tag("sharedNote")
    void shouldKnowIfSharedNoteIsNotSharedWithUser() {
        SharedNote note = sharedNote();

        Assertions.assertFalse(note.isSharedWith("507f1f77bcf86cd799439014"));
    }

    private SharedNote sharedNote() {
        return noteFactory.createSharedNote(noteId, "title", "content", ownerId, sharedWith);
    }
}
