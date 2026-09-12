package com.luispiquinrey.backend.notes.slices.get;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GetNoteRepository {

    Optional<Note> findById(NoteId id);

    List<Note> findByStatus(String status);

    List<Note> findActiveByOwner(String ownerId);

    List<Note> findActiveBySharedWith(String sharedWith);

    List<Note> findActiveByOwnerAndSharedWith(String ownerId, String sharedWith);
}
