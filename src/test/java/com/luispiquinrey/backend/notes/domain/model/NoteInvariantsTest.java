package com.luispiquinrey.backend.notes.domain.model;

import com.luispiquinrey.backend.notes.domain.exception.NoteConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NoteInvariantsTest {
    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("noteInvariants")
    void sharedNotesRejectEveryContentMutation() {
        Note note = new NoteFactory().createSharedNote("note-1", "Original", "Body", "owner", "recipient");
        assertThrows(NoteConflictException.class, () -> note.rename("Changed"));
        assertThrows(NoteConflictException.class, () -> note.changeContent("Changed"));
        assertThrows(NoteConflictException.class, () -> note.replaceTags(List.of(new Tag("changed"))));
        assertEquals("Original", note.title().title());
        assertEquals("Body", note.content().content());
        assertEquals(List.of(), note.tags());
    }

    @Test
    @Timeout(1)
    @org.junit.jupiter.api.Tag("noteInvariants")
    void deletedNotesCannotBePinnedOrEdited() {
        Note note = new NoteFactory().createPrivateNote("note-1", "Original", "Body", "owner");
        note.delete();
        assertThrows(NoteConflictException.class, () -> note.setPinned(true));
        assertThrows(NoteConflictException.class, () -> note.rename("Changed"));
        assertThrows(NoteConflictException.class, () -> note.changeContent("Changed"));
        assertThrows(NoteConflictException.class, () -> note.replaceTags(List.of(new Tag("changed"))));
    }
}
