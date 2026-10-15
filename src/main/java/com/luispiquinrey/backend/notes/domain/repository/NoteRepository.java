package com.luispiquinrey.backend.notes.domain.repository;

import com.luispiquinrey.backend.notes.domain.model.Note;
import com.luispiquinrey.backend.notes.domain.model.NoteId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository {

    Optional<Note> findById(NoteId id);

    void save(Note note);

    List<Note> findByStatus(String status);

    List<Note> findActiveByOwner(String ownerId);

    List<Note> findActiveBySharedWith(String sharedWith);

    List<Note> findActiveByOwnerAndSharedWith(String ownerId, String sharedWith);

    List<Note> findDeletedByOwner(String ownerId);
}
