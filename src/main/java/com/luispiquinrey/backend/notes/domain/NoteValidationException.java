package com.luispiquinrey.backend.notes.domain;

public class NoteValidationException extends RuntimeException {

    public NoteValidationException(String message) {
        super(message);
    }
}
