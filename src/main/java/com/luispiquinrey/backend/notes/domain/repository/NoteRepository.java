package com.luispiquinrey.backend.notes.domain.repository;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import com.luispiquinrey.backend.notes.domain.model.NoteStatus;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository {

    Optional<Note> findById(NoteId id);

    void save(Note note);

    NotePage findAccessibleByStatus(String accountId, NoteStatus status, PageQuery page);

    NotePage findActiveByOwner(String ownerId, PageQuery page);

    NotePage findActiveBySharedWith(String sharedWith, PageQuery page);

    NotePage findActiveByOwnerAndSharedWith(String ownerId, String sharedWith, PageQuery page);

    NotePage findDeletedByOwner(String ownerId, PageQuery page);
}
