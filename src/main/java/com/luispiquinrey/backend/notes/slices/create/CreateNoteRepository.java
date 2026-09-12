package com.luispiquinrey.backend.notes.slices.create;

import com.luispiquinrey.backend.notes.domain.Note;
import org.jmolecules.ddd.annotation.Repository;

@Repository
public interface CreateNoteRepository {
    void save(Note note);
}
