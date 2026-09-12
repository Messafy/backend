package com.luispiquinrey.backend.notes.domain;

public class NoteConflictException extends RuntimeException {

    public NoteConflictException(String message) {
        super(message);
    }
}
