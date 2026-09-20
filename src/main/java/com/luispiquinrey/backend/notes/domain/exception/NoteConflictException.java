package com.luispiquinrey.backend.notes.domain.exception;

public class NoteConflictException extends RuntimeException {

    public NoteConflictException(String message) {
        super(message);
    }
}
