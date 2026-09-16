package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import com.luispiquinrey.backend.notes.domain.NoteStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetNoteService {

    private static final Logger log = LoggerFactory.getLogger(GetNoteService.class);

    private final GetNoteRepository repository;

    public GetNoteService(GetNoteRepository repository) {
        this.repository = repository;
    }

    public Optional<Note> findById(String id) {
        log.debug("Looking up note {}", id);
        Optional<Note> note = repository.findById(new NoteId(id));
        note.ifPresentOrElse(
                found -> log.debug("Found note {} with status {}", found.id().id(), found.status()),
                () -> log.warn("Note {} was not found", id)
        );
        return note;
    }

    public List<Note> findByStatus(NoteStatus status) {
        log.debug("Looking up notes with status {}", status);
        List<Note> notes = repository.findByStatus(status.name());
        log.debug("Found {} notes with status {}", notes.size(), status);
        return notes;
    }

    public List<Note> findActiveByOwner(String ownerId) {
        log.debug("Looking up active notes for owner {}", ownerId);
        List<Note> notes = repository.findActiveByOwner(ownerId);
        log.debug("Found {} active notes for owner {}", notes.size(), ownerId);
        return notes;
    }

    public List<Note> findActiveBySharedWith(String sharedWith) {
        log.debug("Looking up active notes shared with {}", sharedWith);
        List<Note> notes = repository.findActiveBySharedWith(sharedWith);
        log.debug("Found {} active notes shared with {}", notes.size(), sharedWith);
        return notes;
    }

    public List<Note> findActiveByOwnerAndSharedWith(String ownerId, String sharedWith) {
        log.debug("Looking up active notes owned by {} and shared with {}", ownerId, sharedWith);
        List<Note> notes = repository.findActiveByOwnerAndSharedWith(ownerId, sharedWith);
        log.debug("Found {} active notes owned by {} and shared with {}", notes.size(), ownerId, sharedWith);
        return notes;
    }

    public List<Note> findDeletedByOwner(String ownerId) {
        return repository.findDeletedByOwner(ownerId);
    }
}
