package com.luispiquinrey.backend.notes.domain.exception;

public class NoteValidationException extends RuntimeException {

    public NoteValidationException(String message) {
        super(message);
    }
}
