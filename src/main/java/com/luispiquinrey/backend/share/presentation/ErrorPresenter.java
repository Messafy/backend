package com.luispiquinrey.backend.share.presentation;

import org.springframework.stereotype.Component;

@Component
public class ErrorPresenter {
    public ErrorViewModel present(String message, String code) {
        return switch (code) {
            case "VALIDATION_ERROR", "NOT_FOUND_ERROR", "UNAUTHORIZED_ERROR", "FORBIDDEN_ERROR", "CONFLICT_ERROR" ->
                    new ErrorViewModel(message, code);
            default -> new ErrorViewModel("An internal error occurred", "INTERNAL_ERROR");
        };
    }
}
