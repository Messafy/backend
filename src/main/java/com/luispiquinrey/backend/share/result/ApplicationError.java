package com.luispiquinrey.backend.share.result;

import java.util.Objects;

public record ApplicationError(ErrorType type, String message) {
    public ApplicationError {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(message, "message must not be null");
        if (message.isBlank()) {
            throw new IllegalArgumentException("message must not be blank");
        }
    }

    public static ApplicationError validationError(String message) {
        return new ApplicationError(ErrorType.VALIDATION_ERROR, message);
    }

    public static ApplicationError notFoundError(String message) {
        return new ApplicationError(ErrorType.NOT_FOUND_ERROR, message);
    }

    public static ApplicationError unauthorizedError(String message) {
        return new ApplicationError(ErrorType.UNAUTHORIZED_ERROR, message);
    }

    public static ApplicationError forbiddenError(String message) {
        return new ApplicationError(ErrorType.FORBIDDEN_ERROR, message);
    }

    public static ApplicationError internalError(String message) {
        return new ApplicationError(ErrorType.INTERNAL_ERROR, message);
    }

    public static ApplicationError conflictError(String message) {
        return new ApplicationError(ErrorType.CONFLICT_ERROR, message);
    }
}
