package com.luispiquinrey.backend.notes.domain.exception;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(String id) {
        super("note not found: " + id);
    }
}
