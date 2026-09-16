package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.SharedNote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class MarkNoteAsReadService {

    private static final Logger log = LoggerFactory.getLogger(MarkNoteAsReadService.class);

    private final MarkNoteAsReadRepository repository;

    public MarkNoteAsReadService(MarkNoteAsReadRepository repository) {
        this.repository = repository;
    }

    public Note markAsRead(String id, String accountId) {
        log.info("Marking note {} as read for account {}", id, accountId);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> {
                    log.warn("Cannot mark note {} as read because it was not found", id);
                    return new NoteNotFoundException(id);
                });

        if (!canAccess(note, accountId)) {
            log.warn("Account {} is not allowed to mark note {} as read", accountId, id);
            throw new AccessDeniedException("only the owner or the shared recipient can mark a note as read");
        }

        note.markAsRead();
        repository.save(note);
        log.info("Note {} is now read at {}", id, note.readAt());
        return note;
    }

    private boolean canAccess(Note note, String accountId) {
        return note.isOwnedBy(accountId)
                || note instanceof SharedNote sharedNote && sharedNote.isSharedWith(accountId);
    }
}
