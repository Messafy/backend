package com.luispiquinrey.backend.notes.infrastructure;

import com.luispiquinrey.backend.notes.domain.NoteConflictException;
import com.luispiquinrey.backend.notes.domain.NoteNotFoundException;
import com.luispiquinrey.backend.notes.domain.NoteValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class NoteExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(NoteExceptionHandler.class);

    @ExceptionHandler(NoteNotFoundException.class)
    public ProblemDetail handleNotFound(NoteNotFoundException ex) {
        log.warn("Request failed because the note was not found: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NoteValidationException.class)
    public ProblemDetail handleValidation(NoteValidationException ex) {
        log.warn("Request failed because note data was invalid: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoteConflictException.class)
    public ProblemDetail handleConflict(NoteConflictException ex) {
        log.warn("Request failed because the note state does not allow the operation: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.warn("Request failed because access to the note was denied: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }
}
