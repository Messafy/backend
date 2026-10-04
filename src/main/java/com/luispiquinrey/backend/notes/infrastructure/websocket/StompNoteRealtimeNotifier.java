package com.luispiquinrey.backend.notes.infrastructure.websocket;

import com.luispiquinrey.backend.notes.application.port.NoteRealtimeNotifier;
import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.PrivateNote;
import com.luispiquinrey.backend.notes.domain.model.SharedNote;
import com.luispiquinrey.backend.notes.domain.model.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class StompNoteRealtimeNotifier implements NoteRealtimeNotifier {

    private static final Logger log = LoggerFactory.getLogger(StompNoteRealtimeNotifier.class);

    private static final String DESTINATION = "/queue/notes";

    private final SimpMessagingTemplate messagingTemplate;

    public StompNoteRealtimeNotifier(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void notifySharedNote(Note note, String recipientAccountId) {
        log.debug("Notifying account {} about shared note {}", recipientAccountId, note.id().id());
        messagingTemplate.convertAndSendToUser(
                recipientAccountId,
                DESTINATION,
                NotePayload.from(note)
        );
    }

    private record NotePayload(
            String id,
            String title,
            String content,
            String status,
            String type,
            String ownerId,
            String sharedWith,
            List<String> tags,
            boolean pinned,
            Instant createdAt,
            Instant readAt
    ) {

        private static NotePayload from(Note note) {
            String ownerId;
            String sharedWith;
            String type;

            if (note instanceof PrivateNote privateNote) {
                type = "PRIVATE";
                ownerId = privateNote.ownerId().id();
                sharedWith = null;
            } else if (note instanceof SharedNote sharedNote) {
                type = "SHARED";
                ownerId = sharedNote.ownerId().id();
                sharedWith = sharedNote.sharedWith().ownerId().id();
            } else {
                throw new IllegalArgumentException("unsupported note type: " + note.getClass());
            }

            return new NotePayload(
                    note.id().id(),
                    note.title().title(),
                    note.content().content(),
                    note.status().name(),
                    type,
                    ownerId,
                    sharedWith,
                    note.tags().stream().map(Tag::tag).toList(),
                    note.pinned(),
                    note.createdAt().value(),
                    note.readAt() == null ? null : note.readAt().value()
            );
        }
    }
}
