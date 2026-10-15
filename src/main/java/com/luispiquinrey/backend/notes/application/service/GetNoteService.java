package com.luispiquinrey.backend.notes.application.service;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import com.luispiquinrey.backend.notes.domain.repository.NoteRepository;
import com.luispiquinrey.backend.share.identity.UserId;
import com.luispiquinrey.backend.share.logging.Logger;
import com.luispiquinrey.backend.share.logging.LogExecution;

import java.util.List;
import java.util.Optional;

@LogExecution
public class GetNoteService {

    private final Logger log;

    private final NoteRepository repository;

    public GetNoteService(NoteRepository repository, Logger log) {
        this.repository = repository;
        this.log = log;
    }

    public Optional<Note> findById(String id, String authenticatedAccountId) {
        new UserId(authenticatedAccountId);
        log.debug("Loading note {}", id);
        return repository.findById(new NoteId(id))
                .filter(note -> note.canAccess(authenticatedAccountId));
    }

    public List<Note> findByStatus(NoteStatus status, String authenticatedAccountId) {
        new UserId(authenticatedAccountId);
        log.debug("Loading notes with status {}", status);
        return repository.findByStatus(status.name()).stream()
                .filter(note -> note.canAccess(authenticatedAccountId))
                .toList();
    }

    public List<Note> findActiveByOwner(String authenticatedAccountId) {
        new UserId(authenticatedAccountId);
        log.debug("Loading active notes for account {}", authenticatedAccountId);
        return repository.findActiveByOwner(authenticatedAccountId);
    }

    public List<Note> findActiveBySharedWith(String authenticatedAccountId) {
        new UserId(authenticatedAccountId);
        log.debug("Loading active notes shared with {}", authenticatedAccountId);
        return repository.findActiveBySharedWith(authenticatedAccountId);
    }

    public List<Note> findActiveByOwnerAndSharedWith(String authenticatedAccountId, String sharedWith) {
        new UserId(authenticatedAccountId);
        log.debug("Loading active notes for owner {} shared with {}", authenticatedAccountId, sharedWith);
        return repository.findActiveByOwnerAndSharedWith(authenticatedAccountId, sharedWith);
    }

    public List<Note> findDeletedByOwner(String authenticatedAccountId) {
        new UserId(authenticatedAccountId);
        log.debug("Loading deleted notes for owner {}", authenticatedAccountId);
        return repository.findDeletedByOwner(authenticatedAccountId);
    }
}
