package com.luispiquinrey.backend.notes.domain.model;

import org.jmolecules.ddd.annotation.ValueObject;

@ValueObject
public record NoteId(String id) {

    public NoteId {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id cannot be null or blank");
        }
    }
}
