package com.luispiquinrey.backend.notes.slices.delete;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface DeleteNoteRepository {
    Optional<Note> findById(NoteId id);
    void save(Note note);
}
