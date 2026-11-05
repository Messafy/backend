package com.luispiquinrey.backend.share.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import java.util.Map;

public final class Problems {

    public static final String CODE = "code";
    public static final String MESSAGE = "message";

    private static final Map<HttpStatus, String> CODES = Map.of(
            HttpStatus.BAD_REQUEST, "VALIDATION_ERROR",
            HttpStatus.UNAUTHORIZED, "UNAUTHORIZED_ERROR",
            HttpStatus.FORBIDDEN, "FORBIDDEN_ERROR",
            HttpStatus.NOT_FOUND, "NOT_FOUND_ERROR",
            HttpStatus.CONFLICT, "CONFLICT_ERROR"
    );

    private Problems() {
    }

    public static ProblemDetail of(HttpStatusCode status, String code, String message) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, message);
        problem.setProperty(CODE, code);
        problem.setProperty(MESSAGE, message);
        return problem;
    }

    public static ProblemDetail of(HttpStatusCode status, String message) {
        return of(status, codeFor(status), message);
    }

    public static ProblemDetail normalize(ProblemDetail problem) {
        HttpStatusCode status = HttpStatusCode.valueOf(problem.getStatus());
        String message = problem.getDetail() == null ? defaultMessage(status) : problem.getDetail();
        problem.setDetail(message);
        Map<String, Object> properties = problem.getProperties();
        if (properties == null || !properties.containsKey(CODE)) {
            problem.setProperty(CODE, codeFor(status));
        }
        problem.setProperty(MESSAGE, message);
        return problem;
    }

    public static String codeFor(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        if (resolved != null && CODES.containsKey(resolved)) {
            return CODES.get(resolved);
        }
        return status.is4xxClientError() ? "VALIDATION_ERROR" : "INTERNAL_ERROR";
    }

    private static String defaultMessage(HttpStatusCode status) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        return resolved == null ? "Request failed" : resolved.getReasonPhrase();
    }
}
