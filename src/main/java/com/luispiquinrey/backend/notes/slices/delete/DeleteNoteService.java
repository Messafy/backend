package com.luispiquinrey.backend.notes.slices.delete;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DeleteNoteService {

    private static final Logger log = LoggerFactory.getLogger(DeleteNoteService.class);

    private final DeleteNoteRepository repository;

    public DeleteNoteService(DeleteNoteRepository repository) {
        this.repository = repository;
    }

    public void delete(String id) {
        log.info("Deleting note {}", id);
        Note note = repository.findById(new NoteId(id))
                .orElseThrow(() -> {
                    log.warn("Cannot delete note {} because it was not found", id);
                    return new NoteNotFoundException(id);
                });

        note.delete();
        repository.save(note);
        log.info("Note {} marked as deleted", id);
    }
}
