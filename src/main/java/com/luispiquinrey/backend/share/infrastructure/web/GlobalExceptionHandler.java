package com.luispiquinrey.backend.share.infrastructure.web;

import com.luispiquinrey.backend.share.presentation.Problems;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String ERRORS = "errors";
    static final String TIMESTAMP = "timestamp";
    private static final String INTERNAL_MESSAGE = "An internal error occurred";

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> Map.of(
                        "field", err.getField(),
                        "message", err.getDefaultMessage() == null ? "is invalid" : err.getDefaultMessage()
                ))
                .toList();
        log.warn("Request failed because of validation errors: {}", errors);
        ProblemDetail problem = validationProblem(errors);
        return ResponseEntity.badRequest().headers(headers).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<Map<String, String>> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.of(
                        "field", v.getPropertyPath().toString(),
                        "message", v.getMessage()
                ))
                .toList();
        log.warn("Request failed because of constraint violations: {}", errors);
        return validationProblem(errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Request failed because an argument was invalid: {}", ex.getMessage());
        String message = ex.getMessage() == null || ex.getMessage().isBlank() ? "Invalid request" : ex.getMessage();
        return withTimestamp(Problems.of(HttpStatus.BAD_REQUEST, message));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.error("Request failed because of an internal state error", ex);
        return withTimestamp(Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_MESSAGE));
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) throws Exception {
        if (ex instanceof AuthenticationException || ex instanceof AccessDeniedException) {
            throw ex;
        }
        log.error("Request failed because of an unexpected error", ex);
        return withTimestamp(Problems.of(HttpStatus.INTERNAL_SERVER_ERROR, INTERNAL_MESSAGE));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request
    ) {
        ProblemDetail problem = body instanceof ProblemDetail detail
                ? detail
                : ProblemDetail.forStatusAndDetail(statusCode, null);
        if (statusCode.is5xxServerError()) {
            log.error("Request failed with status {}", statusCode.value(), ex);
            problem.setDetail(INTERNAL_MESSAGE);
        } else {
            log.warn("Request failed with status {}: {}", statusCode.value(), problem.getDetail());
        }
        return super.handleExceptionInternal(ex, withTimestamp(Problems.normalize(problem)), headers, statusCode, request);
    }

    private static ProblemDetail validationProblem(List<Map<String, String>> errors) {
        ProblemDetail problem = Problems.of(HttpStatus.BAD_REQUEST, "Request validation failed");
        problem.setProperty(ERRORS, errors);
        return withTimestamp(problem);
    }

    private static ProblemDetail withTimestamp(ProblemDetail problem) {
        problem.setProperty(TIMESTAMP, Instant.now());
        return problem;
    }
}
