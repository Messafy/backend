package com.luispiquinrey.backend.notes.domain;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String id) {
        super("note not found: " + id);
    }
}
