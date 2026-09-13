package com.luispiquinrey.backend.notes.slices.update;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface UpdateNoteRepository {
    Optional<Note> findById(NoteId id);

    void save(Note note);
}
