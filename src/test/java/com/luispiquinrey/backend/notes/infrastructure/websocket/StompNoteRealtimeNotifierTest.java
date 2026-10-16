package com.luispiquinrey.backend.notes.infrastructure.websocket;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteFactory;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class StompNoteRealtimeNotifierTest {
    private final SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
    private final StompNoteRealtimeNotifier notifier = new StompNoteRealtimeNotifier(template);

    @Test
    void deliversSharedNotePayloadToRecipient() {
        Note note = new NoteFactory().createSharedNote(
                "note", "Title", "Content", "owner", "recipient", List.of(new Tag("work")));
        note.setPinned(true);
        note.markAsRead();

        JsonNode payload = deliver(note);

        assertEquals("SHARED", payload.get("type").asText());
        assertEquals("recipient", payload.get("sharedWith").asText());
        assertEquals("READ", payload.get("status").asText());
        assertEquals("work", payload.get("tags").get(0).asText());
        assertTrue(payload.get("pinned").asBoolean());
        assertEquals(note.readAt().value().toString(), payload.get("readAt").asText());
    }

    @Test
    void deliversPrivateNoteWithoutRecipientOrReadTimestamp() {
        Note note = new NoteFactory().createPrivateNote("note", "Title", "Content", "owner");

        JsonNode payload = deliver(note);

        assertEquals("PRIVATE", payload.get("type").asText());
        assertTrue(payload.get("sharedWith").isNull());
        assertTrue(payload.get("readAt").isNull());
        assertTrue(payload.get("tags").isEmpty());
        assertFalse(payload.get("pinned").asBoolean());
    }

    @Test
    void rejectsUnsupportedNoteTypeWithoutSending() {
        Note note = mock(Note.class);
        when(note.id()).thenReturn(new NoteId("note"));

        assertThrows(IllegalArgumentException.class, () -> notifier.notifySharedNote(note, "recipient"));

        verifyNoInteractions(template);
    }

    private JsonNode deliver(Note note) {
        notifier.notifySharedNote(note, "recipient");
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSendToUser(eq("recipient"), eq("/queue/notes"), captor.capture());
        JsonNode payload = JsonMapper.builder().build().valueToTree(captor.getValue());
        assertEquals("note", payload.get("id").asText());
        assertEquals("Title", payload.get("title").asText());
        assertEquals("Content", payload.get("content").asText());
        assertEquals("owner", payload.get("ownerId").asText());
        assertEquals(note.createdAt().value().toString(), payload.get("createdAt").asText());
        return payload;
    }
}
