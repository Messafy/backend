package com.luispiquinrey.backend.notes.domain.exception;

public class NoteAccessDeniedException extends RuntimeException {
    public NoteAccessDeniedException(String message) {
        super(message);
    }
}
