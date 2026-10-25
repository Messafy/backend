package com.luispiquinrey.backend.share.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class HttpResponsePresenter {

    public <T> ResponseEntity<?> toHttp(OperationResult<T> operation) {
        return toHttp(operation, ResponseEntity::ok);
    }

    public <T> ResponseEntity<?> toHttp(
            OperationResult<T> operation,
            Function<T, ResponseEntity<?>> onSuccess
    ) {
        if (operation.isSuccess()) {
            return onSuccess.apply(operation.getValue());
        }

        ErrorViewModel error = operation.getError();
        HttpStatus status = switch (error.code()) {
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            case "NOT_FOUND_ERROR" -> HttpStatus.NOT_FOUND;
            case "UNAUTHORIZED_ERROR" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN_ERROR" -> HttpStatus.FORBIDDEN;
            case "CONFLICT_ERROR" -> HttpStatus.CONFLICT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
        return ResponseEntity.status(status).body(error);
    }
}
