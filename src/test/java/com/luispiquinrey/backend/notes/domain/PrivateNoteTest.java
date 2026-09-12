package com.luispiquinrey.backend.notes.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/*
 * This class also integrates the tests for behavior inherited from the abstract Note class.
 * They are kept here to avoid duplicating the same abstract note behavior checks in SharedNoteTest.
 */
public class PrivateNoteTest {
    private final NoteFactory noteFactory = new NoteFactory();
    private final String noteId = "507f1f77bcf86cd799439011";
    private final String ownerId = "507f1f77bcf86cd799439012";

    @Test
    @Timeout(1)
    @Tag("privateNote")
    void shouldCreatePrivateNoteWithInitialState() {
        PrivateNote note = privateNote();

        Assertions.assertEquals(noteId, note.id().id());
        Assertions.assertEquals("title", note.title().title());
        Assertions.assertEquals("content", note.content().content());
        Assertions.assertEquals(ownerId, note.ownerId().id());
        Assertions.assertEquals(NoteStatus.NEW, note.status());
        Assertions.assertNotNull(note.createdAt());
        Assertions.assertNull(note.readAt());
        Assertions.assertNull(note.hiddenAt());
        Assertions.assertNull(note.reportedAt());
    }

    @Test
    @Timeout(1)
    @Tag("privateNote")
    void shouldKnowIfPrivateNoteIsOwnedByUser() {
        PrivateNote note = privateNote();

        Assertions.assertTrue(note.isOwnedBy(ownerId));
    }

    @Test
    @Timeout(1)
    @Tag("privateNote")
    void shouldKnowIfPrivateNoteIsNotOwnedByUser() {
        PrivateNote note = privateNote();

        Assertions.assertFalse(note.isOwnedBy("507f1f77bcf86cd799439013"));
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldRenameNote() {
        Note note = privateNote();

        note.rename("new title");

        Assertions.assertEquals("new title", note.title().title());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldChangeNoteContent() {
        Note note = privateNote();

        note.changeContent("new content");

        Assertions.assertEquals("new content", note.content().content());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldMarkNoteAsRead() {
        Note note = privateNote();

        note.markAsRead();

        Assertions.assertEquals(NoteStatus.READ, note.status());
        Assertions.assertNotNull(note.readAt());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldHideNote() {
        Note note = privateNote();

        note.hide();

        Assertions.assertEquals(NoteStatus.HIDDEN, note.status());
        Assertions.assertNotNull(note.hiddenAt());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldReportNote() {
        Note note = privateNote();

        note.report();

        Assertions.assertEquals(NoteStatus.REPORTED, note.status());
        Assertions.assertNotNull(note.reportedAt());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldDeleteNote() {
        Note note = privateNote();

        note.delete();

        Assertions.assertEquals(NoteStatus.DELETED, note.status());
    }

    @Test
    @Timeout(1)
    @Tag("note")
    void shouldThrowWhenDeletedNoteIsMarkedAsRead() {
        Note note = privateNote();

        note.delete();

        Assertions.assertThrows(NoteConflictException.class, note::markAsRead);
    }

    private PrivateNote privateNote() {
        return noteFactory.createPrivateNote(noteId, "title", "content", ownerId);
    }

}
