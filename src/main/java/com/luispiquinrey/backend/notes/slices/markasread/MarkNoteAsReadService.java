package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MarkNoteAsReadService {

    private static final Logger log = LoggerFactory.getLogger(MarkNoteAsReadService.class);

    private final MarkNoteAsReadRepository repository;

    public MarkNoteAsReadService(MarkNoteAsReadRepository repository) {
        this.repository = repository;
    }

    public Note markAsRead(String id) {
        log.info("Marking note {} as read", id);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> {
                    log.warn("Cannot mark note {} as read because it was not found", id);
                    return new NoteNotFoundException(id);
                });

        note.markAsRead();
        repository.save(note);
        log.info("Note {} is now read at {}", id, note.readAt());
        return note;
    }
}
