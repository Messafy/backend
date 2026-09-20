package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GetNoteService {

    private final NoteRepository repository;

    public GetNoteService(NoteRepository repository) {
        this.repository = repository;
    }

    public Optional<Note> findById(String id) {
        return repository.findById(new NoteId(id));
    }

    public List<Note> findByStatus(NoteStatus status) {
        List<Note> notes = repository.findByStatus(status.name());
        return notes;
    }

    public List<Note> findActiveByOwner(String ownerId) {
        List<Note> notes = repository.findActiveByOwner(ownerId);
        return notes;
    }

    public List<Note> findActiveBySharedWith(String sharedWith) {
        List<Note> notes = repository.findActiveBySharedWith(sharedWith);
        return notes;
    }

    public List<Note> findActiveByOwnerAndSharedWith(String ownerId, String sharedWith) {
        List<Note> notes = repository.findActiveByOwnerAndSharedWith(ownerId, sharedWith);
        return notes;
    }

    public List<Note> findDeletedByOwner(String ownerId) {
        return repository.findDeletedByOwner(ownerId);
    }
}
