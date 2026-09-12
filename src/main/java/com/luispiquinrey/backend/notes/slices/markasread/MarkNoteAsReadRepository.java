package com.luispiquinrey.backend.notes.slices.markasread;

import com.luispiquinrey.backend.notes.domain.Note;
import com.luispiquinrey.backend.notes.domain.NoteId;
import org.jmolecules.ddd.annotation.Repository;

import java.util.Optional;

@Repository
public interface MarkNoteAsReadRepository {

    Optional<Note> findById(NoteId id);

    void save(Note note);
}
